package sanity.nil.meta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Status;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.hibernate.query.Order;
import sanity.nil.grpc.block.BlockService;
import sanity.nil.grpc.block.CheckBlocksExistenceRequest;
import sanity.nil.grpc.block.Code;
import sanity.nil.grpc.block.DeleteBlocksRequest;
import sanity.nil.meta.cache.FileMetadataCache;
import sanity.nil.meta.cache.SubscriptionQuotaCache;
import sanity.nil.meta.cache.model.FileMetadata;
import sanity.nil.meta.consts.*;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlocksMetadata;
import sanity.nil.meta.dto.file.*;
import sanity.nil.meta.exceptions.CryptoException;
import sanity.nil.meta.exceptions.InsufficientQuotaException;
import sanity.nil.meta.exceptions.InvalidParametersException;
import sanity.nil.meta.mappers.FileMapper;
import sanity.nil.meta.model.*;
import sanity.nil.meta.security.LinkEncoder;
import sanity.nil.security.Identity;
import sanity.nil.security.Role;
import sanity.nil.security.WorkspaceIdentityProvider;
import sanity.nil.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static sanity.nil.meta.consts.Constants.*;

@JBossLog
@ApplicationScoped
public class MetadataService {

    @Inject
    UserTransaction userTransaction;
    @Inject
    FileJournalRepo fileJournalRepo;
    @Inject
    EntityManager entityManager;
    @Inject
    WorkspaceIdentityProvider identityProvider;
    @Inject
    SubscriptionQuotaCache subscriptionQuotaCache;
    @Inject
    FileMetadataCache fileMetadataCache;
    @Inject
    LinkEncoder linkEncoder;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    FileMapper fileMapper;
    @GrpcClient("blockService")
    BlockService blockClient;

    public List<String> getFileBlockList(String fileID, String workspaceID, Integer versions) {
        log.info("MetadataService thread: " + Thread.currentThread().getName());
        var blocklist = entityManager.createQuery("SELECT f.blocklist FROM FileJournalModel f " +
                        "WHERE f.id.fileID = :fileID AND f.id.workspaceID = :wsID " +
                        "ORDER BY f.historyID DESC ", String.class)
                .setParameter("fileID", fileID)
                .setParameter("wsID", workspaceID)
                .setMaxResults(versions)
                .getSingleResult();

        return getBlocksFromBlockList(blocklist);
    }

    @Transactional
    public void deleteFile(String fileIDParam, String workspaceIDParam) {
        var identity = identityProvider.getIdentity(workspaceIDParam);
        if (!identity.hasRole(Role.USER)) {
            throw new UnauthorizedException();
        }

        var user = entityManager.find(UserModel.class, identity.getUserID());
        entityManager.createQuery("UPDATE FileJournalModel f SET f.state = :newState, " +
                        "f.updatedBy = :updatedBy " +
                        "WHERE f.id.fileID = :fileID AND f.id.workspaceID = :wsID")
                .setParameter("newState", FileState.DELETED)
                .setParameter("updatedBy", user)
                .setParameter("fileID", fileIDParam)
                .setParameter("wsID", workspaceIDParam)
                .executeUpdate();
        var metadata = objectMapper.createObjectNode();
        metadata.put("workspace",workspaceIDParam);
        var performAt = LocalDateTime.now().plusDays(Constants.FILE_RETENTION_DAYS);
        var task = new TaskModel(fileIDParam, TaskType.DELETE_FILE, metadata, performAt, TaskStatus.CREATED);
        entityManager.persist(task);
    }

    //    @CircuitBreaker()
    @Retry(maxRetries = 3, delay = 100, delayUnit = ChronoUnit.MILLIS)
    public void deleteBlocksWithRetry(List<String> blockList) {

        var response = blockClient.deleteBlocks(DeleteBlocksRequest.newBuilder()
                .addAllHash(blockList).build()
        ).await().atMost(Duration.ofSeconds(2L));
        if (response.getCode().equals(Code.failure)) {
            throw new RuntimeException("Error calling delete blocks");
        }
    }

//    @Transactional
    public void deleteFileJournal(Long fileID, Long workspaceID) {
        var deleted = entityManager.createQuery("DELETE FROM FileJournalModel f " +
                        "WHERE f.id.fileID = :fileID AND f.id.workspaceID = :wsID ")
                .setParameter("fileID", fileID)
                .setParameter("wsID", workspaceID)
                .executeUpdate();
    }

    public BlockMetadata getBlocksMetadata(GetBlocksMetadata request) {
        var identity = identityProvider.getIdentity(String.valueOf(request.workspaceID()));
        if (!identity.hasRole(Role.USER)) {
            throw new UnauthorizedException();
        }
        var userUploader = entityManager.find(UserModel.class, identity.getUserID());

        var requestSortedBlocks = request.blocks().stream()
                .sorted(Comparator.comparing(a -> a.position()))
                .map(e -> e.hash())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Collection<String> missingBlocks = new ArrayList<>();

        // get from cache and check for block differences
        var cachedMetadataList = fileMetadataCache.getByParams(request.workspaceID(), null, request.path(), 1);
        FileMetadata cachedMetadata = null;
        if (CollectionUtils.isEmpty(cachedMetadataList)) {
            var fileInUploadOp = fileJournalRepo.findLatestByPathAndState(request.workspaceID(), request.path(), FileState.IN_UPLOAD);
            if (fileInUploadOp.isPresent()) {
                cachedMetadata = fileMapper.journalToMetadata(fileInUploadOp.get());
            }
        } else {
            cachedMetadata = cachedMetadataList.getFirst();
        }
        if (cachedMetadata != null && cachedMetadata.state().equals(FileState.UPLOADED)) {
            var existingBlocks = cachedMetadata.blocks();
            var index = 0;
            for (String block : requestSortedBlocks) {
                if (index >= existingBlocks.size()) {
                    missingBlocks.add(block);
                } else if (!existingBlocks.get(index).equals(block)) {
                    missingBlocks.add(block);
                }
                index++;
            }
            // or ask block client for blocks metadata
        } else {
            try {
                log.info("checking blocks: " + requestSortedBlocks.toString());
                var response = blockClient.checkBlocksExistence(CheckBlocksExistenceRequest.newBuilder()
                        .addAllHash(requestSortedBlocks).build()
                ).await().atMost(Duration.ofSeconds(6L));
                missingBlocks = response.getMissingBlocksList();
                if (CollectionUtils.isNotEmpty(missingBlocks)) {
                    log.info("got missing blocks: " + missingBlocks.toString());
                }
            } catch (Exception e) {
                log.error("Error checking blocks existence", e);
                throw new RuntimeException("Internal server error");
            }
        }

        var existsInAnotherWorkspace = false;
        if (missingBlocks.isEmpty()) {
            log.debug("All blocks already exist");
            var existingFileOp = updateFileState(identity.getUserID(), request.workspaceID(), request.path());
            if (existingFileOp.isPresent()) {
                var existingFile = existingFileOp.get();
                var cacheEntry = fileMapper.journalToMetadata(existingFile);
                fileMetadataCache.persistFileMetadata(cacheEntry, request.workspaceID(), existingFile.getFileID(), Duration.ofMinutes(10L));

                var fileInfo = fileMapper.journalToInfo(existingFile);
                return new BlockMetadata(request.correlationID(), null, fileInfo);
            } else {
                // forse insert because file exists in another workspace
                missingBlocks = requestSortedBlocks;
                existsInAnotherWorkspace = true;
            }
        }
        var fileSize = calculateFileSize(missingBlocks.size(), request.lastBlockSize());
        verifyQuotaUsage(identity.getUserID(), userUploader.getSubscription().getId(), fileSize);

        var workspace = getWorkspace(request.workspaceID(), identity.getUserID()).get();

        var file = new FileJournalModel();
        if (cachedMetadata != null && cachedMetadata.state().equals(FileState.IN_UPLOAD)) {
            // if file is not uploaded yet (some blocks weren't delivered) - resumable upload
            return new BlockMetadata(request.correlationID(), missingBlocks, null);
        } else if (existsInAnotherWorkspace) {
            file = postProcessExistingFileNewInWorkspace(request.path(), userUploader, missingBlocks, workspace, fileSize);
            missingBlocks = null;
        } else if (missingBlocks.size() == requestSortedBlocks.size()) {
            file = postProcessNewFile(request.path(), userUploader, missingBlocks, workspace, fileSize);
        } else {
            file = postProcessUpdateFile(request.path(), userUploader, requestSortedBlocks, workspace, fileSize);
        }

        var cacheEntry = fileMapper.journalToMetadata(file);
        fileMetadataCache.persistFileMetadata(cacheEntry, request.workspaceID(), file.getFileID(), Duration.ofMinutes(10L));

        return new BlockMetadata(request.correlationID(), missingBlocks, fileMapper.journalToInfo(file));
    }

    @Transactional
    protected FileJournalModel postProcessExistingFileNewInWorkspace(String path, UserModel userUploader, Collection<String> newBlocks, WorkspaceModel workspace, Long fileSize) {
        // separate method because not sure how to treat such case, logic can be rethought
        FileJournalModel fileJournal = new FileJournalModel(entityManager.merge(workspace), path, userUploader, FileState.UPLOADED, (short) 1,
                fileSize, createBlockList(newBlocks.stream()));
        fileJournalRepo.insert(fileJournal);
        var userStatistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
                identityProvider.getCheckedIdentity().getUserID(), Quota.USER_STORAGE_USED.id())
        );
        userStatistics.setValue(String.valueOf(
                Long.parseLong(userStatistics.getValue()) + fileSize)
        );
        entityManager.persist(userStatistics);
        return fileJournal;
    }

    @Transactional
    protected FileJournalModel postProcessNewFile(String path, UserModel userUploader, Collection<String> newBlocks, WorkspaceModel workspace, Long fileSize) {
        FileJournalModel fileJournal = new FileJournalModel(entityManager.merge(workspace), path, userUploader, FileState.IN_UPLOAD, (short) 1,
                fileSize, createBlockList(newBlocks.stream()));
        fileJournalRepo.insert(fileJournal);
        var userStatistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
                identityProvider.getCheckedIdentity().getUserID(), Quota.USER_STORAGE_USED.id())
        );
        userStatistics.setValue(String.valueOf(
                Long.parseLong(userStatistics.getValue()) + fileSize)
        );
        entityManager.persist(userStatistics);
        return fileJournal;
    }

    @Transactional
    protected FileJournalModel postProcessUpdateFile(String path, UserModel userUploader, Set<String> newBlocks, WorkspaceModel workspace, Long fileSize) {
        var existingVersions = entityManager.createQuery("SELECT f.historyID FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND f.path = :path " +
                        "AND f.state <> :state " +
                        "ORDER BY f.historyID", Integer.class)
                .setParameter("wsID", workspace.getId())
                .setParameter("path", path)
                .setParameter("state", FileState.DELETED)
                .getResultList();

        if (existingVersions.size() == FILE_VERSIONS_MAX) {
            // TODO: delete blocks for file versions that are cleaned up
            // or to maintain a policy of soft deletes?
            entityManager.createQuery("DELETE FROM FileJournalModel fj " +
                            "WHERE fj.id.workspaceID = :wsID AND fj.historyID = :version")
                    .setParameter("wsID", workspace.getId())
                    .setParameter("version", existingVersions.getFirst()) // the oldest version
                    .executeUpdate();
        }

        // unmark previous version as latest
        entityManager.createQuery("UPDATE FileJournalModel f SET latest = 0 " +
                "WHERE f.historyID = :version AND f.id.workspaceID = :wsID")
                .setParameter("version", existingVersions.getLast())
                .setParameter("wsID", workspace.getId())
                .executeUpdate();

        var fileJournal = entityManager.createQuery("SELECT fj FROM FileJournalModel fj " +
                        "WHERE fj.id.workspaceID = ?1 AND fj.path = ?2 " +
                        "ORDER BY fj.historyID DESC", FileJournalModel.class)
                .setParameter(1, workspace.getId())
                .setParameter(2, path)
                .setMaxResults(1)
                .getSingleResult();

        var newFileJournalEntry = new FileJournalModel(entityManager.merge(workspace), path, userUploader,
                FileState.IN_UPLOAD, (short) 1, fileJournal.getSize() + fileSize, createBlockList(newBlocks.stream())
        );
        newFileJournalEntry.setUpdatedBy(userUploader);
        fileJournalRepo.insert(newFileJournalEntry);

        log.debug("FileJournal entry history updated to: " + fileJournal.getHistoryID());
        var userStatistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
                identityProvider.getCheckedIdentity().getUserID(), Quota.USER_STORAGE_USED.id())
        );
        userStatistics.setValue(String.valueOf(
                Long.parseLong(userStatistics.getValue()) + fileSize)
        );
        entityManager.persist(userStatistics);
        return newFileJournalEntry;
    }

    @Transactional
    protected Optional<FileJournalModel> updateFileState(UUID userID, Long workspaceID, String path) {
        var user = entityManager.find(UserModel.class, userID);
        FileJournalModel uploadedFile;
        try {
             uploadedFile = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                            "WHERE f.id.workspaceID = :wsID AND f.path = :path " +
                            "ORDER BY f.historyID DESC " +
                            "FETCH FIRST 1 ROW ONLY ", FileJournalModel.class)
                    .setParameter("wsID", workspaceID)
                    .setParameter("path", path)
                    .getSingleResult();
        } catch (NoResultException e) {
            log.info("Same file in another workspace identified, proceeding to creating a new entry");
            return Optional.empty();
        }

        if (uploadedFile.getState().equals(FileState.IN_UPLOAD)) {
            uploadedFile.setState(FileState.UPLOADED);
            uploadedFile.setUpdatedBy(user);
            entityManager.merge(uploadedFile);
            log.debug("File was uploaded before, now updated state");
        }
        return Optional.of(uploadedFile);
    }

    public Optional<WorkspaceModel> getWorkspace(Long wsID, UUID userID) {
        var workspaces = entityManager.createQuery("SELECT m.workspace FROM UserWorkspaceModel m " +
                        "WHERE m.id.workspaceID = ?1 AND m.id.userID IN ?2 ", WorkspaceModel.class)
                .setParameter(1, wsID)
                .setParameter(2, userID)
                .getResultList();
        if (workspaces == null || workspaces.isEmpty())
            return Optional.empty();

        return Optional.of(workspaces.getFirst());
    }

    /**
     * Link
     * @param fileID has to be provided if access made by user with permanent access
     * @param wsID has to be provided if access made by user with permanent access
     * @param link has to be provided if access made by external guest user
     * @param listVersions used when needs to get info about file versions
     * @param version used to get a specific version of a file
     * @return FileInfo
     */
    public FileInfo getFileInfo(Long fileID, Long wsID, String link, String path, Boolean listVersions, Integer version) {
        if (!hasFileAccessPermissions(wsID, link)) {
            throw new ForbiddenException();
        }
        if ((fileID == null || wsID == null) && StringUtils.isEmpty(link)) {
            throw new InvalidParametersException("Either link for file access is invalid or workspace/file id wasn't provided");
        }

        if (version != null && StringUtils.isNotBlank(path)) {
            var fileOp = fileJournalRepo.findByPathAndVersion(wsID, path, version);
            if (fileOp.isEmpty()) {
                throw new NotFoundException();
            }
            log.debug("Returning file info for specific version");
            return fileMapper.journalToInfo(fileOp.get());
        }

        // TODO: optimize reads by not selecting not needed columns (blocklist)
        var fileOp = fileJournalRepo.findByIdAndStateIn(fileID, wsID, FileState.UPLOADED);
        if (fileOp.isEmpty()) {
            throw new NotFoundException();
        }
        var file = fileOp.get();

        if (listVersions && (StringUtils.isNotBlank(path) || fileID != null)) {
            if (StringUtils.isEmpty(path)) {
                path = fileJournalRepo.findPathByID(wsID, fileID);
            }
            var versions = fileJournalRepo.getVersionsByPath(wsID, path);
            var versionedFile = fileMapper.journalToVersionedInfo(file);
            versionedFile.versions = IntStream.range(1, versions.size()+1).mapToObj(v -> {
                return new FileVersion(v, versions.get(v-1));
            }).toList();
            log.debug("Returning file info with versions");
            return versionedFile;
        }

        return fileMapper.journalToInfo(file);
    }

    /**
    * Returns a paginated list of information about files
     **/
    public Paged<FileInfo> searchFiles(FileFilters filters) {
        Identity identity;
        if (filters.wsID() != null) {
            identity = identityProvider.getIdentity(String.valueOf(filters.wsID()));
        } else {
            identity = identityProvider.getCheckedIdentity();
        }
        if (!identity.hasRole(Role.USER)) {
            throw new ForbiddenException();
        }
        var page = filters.page() == null ? 0 : filters.page();
        var size = filters.size() == null ? 10 : filters.size();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FileJournalModel> selectQuery = cb.createQuery(FileJournalModel.class);
        Root<FileJournalModel> selectRoot = selectQuery.from(FileJournalModel.class);

        var selectPredicates = fileJournalRepo.buildPredicatesFromParams(cb, selectRoot, filters);

        selectQuery.select(selectRoot).where(cb.and(selectPredicates.toArray(new Predicate[0]))).orderBy(cb.desc(selectRoot.get("updatedAt")));

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FileJournalModel> countRoot = countQuery.from(FileJournalModel.class);
        var countPredicates = fileJournalRepo.buildPredicatesFromParams(cb, countRoot, filters);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        var filesFound = entityManager.createQuery(selectQuery).setFirstResult(page*size).setMaxResults(size).getResultList();
        var filesCount = entityManager.createQuery(countQuery).getSingleResult();

        List<FileInfo> res;

        if (filters.deleted() == null || !filters.deleted()) {
            res = filesFound.stream()
                    .map(fileMapper::journalToInfo)
                    .collect(Collectors.toList());
        } else {
            res = filesFound.stream()
                    .map(fileMapper::journalToDeletedFileInfo)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        int totalPages = (int) Math.ceil((double) filesCount / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        return new Paged<FileInfo>().of(res, totalPages, hasNext, hasPrevious);
    }

    public Paged<FileInfo> listDirectory(String directory, Long wsID, Integer page, Integer size) {
        var identity = identityProvider.getIdentity(String.valueOf(wsID));
        if (!identity.hasRole(Role.USER)) {
            throw new ForbiddenException();
        }
        page = page == null ? 0 : page;
        size = size == null ? 10 : size;
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FileJournalModel> selectQuery = cb.createQuery(FileJournalModel.class);
        Root<FileJournalModel> selectRoot = selectQuery.from(FileJournalModel.class);

        var selectPredicates = fileJournalRepo.buildPredicatesFromParams(cb, selectRoot, wsID, directory);

        selectQuery.select(selectRoot).where(cb.and(selectPredicates.toArray(new Predicate[0]))).orderBy(cb.desc(selectRoot.get("updatedAt")));

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FileJournalModel> countRoot = countQuery.from(FileJournalModel.class);
        var countPredicates = fileJournalRepo.buildPredicatesFromParams(cb, countRoot, wsID, directory);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        var filesFound = entityManager.createQuery(selectQuery).setFirstResult(page*size).setMaxResults(size).getResultList();
        var filesCount = entityManager.createQuery(countQuery).getSingleResult();
        // TODO: how to handle if a file has more than 1 version?
        List<FileInfo> res = filesFound.stream()
                .map(fileMapper::journalToInfo)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) filesCount / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        return new Paged<FileInfo>().of(res, totalPages, hasNext, hasPrevious);
    }

    public FileNode listFileTree(Long wsID, String pathParam) {
        var path = pathParam == null ? "/" : pathParam;
        var nodes = fileJournalRepo.getFileNodesByFilters(wsID, path);
        nodes.forEach(f -> f.isDirectory = f.name.charAt(f.name.length()-1) == DIRECTORY_CHAR);
        return buildTree(nodes, path);
    }

    public FileNode buildTree(List<FileInfo> files, String path) {
        FileNode root = new FileNode();
        root.name = path == null ? "/" : path;
        // find root, get its info and delete it from files
        files.stream().filter(f -> f.name.equals(root.name)).findFirst().ifPresent(f -> {
            root.fileInfo = f;
            files.remove(f);
        });

        // find dirs
        List<FileInfo> dirFilesDelete = new ArrayList<>();
        var directories = files.stream().filter(f -> f.isDirectory)
                .peek(dirFilesDelete::add)
                .collect(Collectors.groupingBy(f -> f.name.split(DIRECTORY_CHAR.toString()).length-1, // first '/' doesnt count
                        Collectors.mapping(f -> f, Collectors.toList()))
                );
        files.removeAll(dirFilesDelete);

        if (directories.isEmpty()) {
            files.stream()
                    .map(f -> fileToNode(f, root.name))
                    .forEach(node -> root.children.add(node));
            return root;
        }
        var prevLevel = List.of(root);
        for (Map.Entry<Integer, List<FileInfo>> directoryLevel : directories.entrySet()) {
            for (FileInfo directory : directoryLevel.getValue()) {
                var prevLevelDir = prevLevel.stream().filter(d -> directory.name.startsWith(d.name)).findFirst().get();
                var dir = fileToNode(directory, prevLevelDir.name);
                var dirFiles = files.stream().filter(f -> f.name.substring(0, f.name.lastIndexOf(DIRECTORY_CHAR)+1).equals(directory.name)).toList();
                files.removeAll(dirFiles);
                dirFiles.stream().map(e -> fileToNode(e, directory.name))
                        .forEach(node -> dir.children.add(node));
                prevLevelDir.children.add(dir);
            }
            prevLevel = prevLevel.stream()
                    .filter(e -> CollectionUtils.isNotEmpty(e.children))
                    .flatMap(e -> e.children.stream())
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(files)) {
            files.stream()
                    .map(f -> fileToNode(f, root.name))
                    .forEach(rootNode -> root.children.add(rootNode));
        }
        return root;
    }

    private FileNode fileToNode(FileInfo fileInfo, String dir) {
        return new FileNode(StringUtils.substringAfter(fileInfo.name, dir.substring(0, dir.length()-1)), fileInfo);
    }

    public FileInfo createDirectory(CreateDirectory request) {
        var identity = identityProvider.getIdentity(String.valueOf(request.workspaceID()));
        if (!identity.hasRole(Role.USER)) {
            throw new UnauthorizedException();
        }
        FileJournalModel newDirectory = null;
        var dir = request.path() + request.name() + "/";
        try {
            var externalTransaction = true;
            if (userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION) {
                userTransaction.begin();
                externalTransaction = false;
            }
            var workspace = getWorkspace(request.workspaceID(), identity.getUserID());
            var userUploader = entityManager.find(UserModel.class, identity.getUserID());

            newDirectory = new FileJournalModel(workspace.get(), dir, userUploader, FileState.UPLOADED, (short) 1, 0L, null);
            fileJournalRepo.insert(newDirectory);
            if (!externalTransaction) {
                userTransaction.commit();
            }
        } catch (Exception e) {
            log.error("Error creating directory", e);
            throw new RuntimeException("Application error");
        }
        return fileMapper.journalToInfo(newDirectory);
    }

    public List<String> getDirectories(Long wsID, String path) {
        var directoriesInPath = path == null ? "%" : path + "%";
        return entityManager.createQuery("SELECT f.path FROM FileJournalModel f " +
                "WHERE f.id.workspaceID = :wsID AND f.path LIKE :path AND f.size = 0", String.class)
                .setParameter("wsID", wsID)
                .setParameter("path", directoriesInPath)
                .getResultList();
    }

    private boolean hasFileAccessPermissions(Long wsID, String link) {
        if (StringUtils.isNotEmpty(link)) {
            var verifiedLink = verifyLink(link);
            return verifiedLink.valid() && !verifiedLink.expired();
        } else {
            var identity = identityProvider.getIdentity(String.valueOf(wsID));
            return identity.hasRole(Role.USER);
        }
    }

    public boolean existsUserWorkspace(UUID userID, String workspaceID) {
        return getWorkspace(Long.valueOf(workspaceID), userID).isPresent();
    }

    @Transactional
    public String constructLinkForSharing(Long fileID, Long wsID, Long expiresAt) {
        // TODO: include device identification (user-agent or fingerprint) in link construction
        var identity = identityProvider.getIdentity(String.valueOf(wsID));
        String link = String.format("%s:%s:%s:%s", identity.getUserID(), fileID, wsID, expiresAt);

        String encryptedLink = "";
        try {
            encryptedLink = linkEncoder.encrypt(link);
        } catch (CryptoException e) {
            log.error("Could not encrypt link " + link, e);
            return "";
        }
        LinkModel linkModel = new LinkModel(identity.getUserID(), link, LocalDateTime.now());
        entityManager.persist(linkModel);

        return encryptedLink;
    }

    @Transactional
    public String updateFile(Long fileID, Long wsID, String newName, FileAction fileAction) {
        // TODO: make so that each file has a parent except of a root directory '/', so that when a folder
        // is renamed, only it has to be changed, also search will be a lot easier
        var identity = identityProvider.getIdentity(String.valueOf(wsID));

        var fileJournalOp = fileJournalRepo.findById(fileID, wsID);
        if (fileJournalOp.isEmpty()) {
            throw new NotFoundException();
        }
        var fileJournal = fileJournalOp.get();
        if (!fileJournal.getUploader().getId().equals(identity.getUserID())) {
            throw new UnauthorizedException("File wasn't uploaded by current user");
        }
        FileState updatedState = null;
        String updatedName = null;

        if (StringUtils.isNotBlank(newName)) {
            if (isFileDirectory(fileJournal.getPath())) {
                updatedName = newName;
            } else {
                var currPath = StringUtils.substringBeforeLast(fileJournal.getPath(), "/");
                var extention = StringUtils.substringAfterLast(fileJournal.getPath(), ".");
                updatedName = String.format("%s/%s.%s", currPath, newName, extention);
            }
        }
        if (fileJournal.getState().equals(FileState.DELETED)) {
            List<TaskModel> deletionTasks = entityManager.createNativeQuery("SELECT * FROM metadata_db.tasks t " +
                            "WHERE t.object_id = :fileID AND metadata ->> 'workspace' = :workspaceID", TaskModel.class)
                    .setParameter("fileID", String.valueOf(fileID))
                    .setParameter("workspaceID", String.valueOf(wsID))
                    .getResultList();
            Optional<TaskModel> deletionTask = Optional.empty();
            if (CollectionUtils.isNotEmpty(deletionTasks)) {
                deletionTask = Optional.of(deletionTasks.getFirst());
            }
            if (fileAction.equals(FileAction.RESTORE)) {
                updatedState = FileState.UPLOADED;
                deletionTask.ifPresent(task -> entityManager.remove(task));
            }
            if (fileAction.equals(FileAction.DELETE_FOREVER)) {
                updatedState = FileState.DELETING;
                deletionTask.ifPresent(task -> {
                    task.setPerformAt(LocalDateTime.now());
                    entityManager.persist(task);
                });
                var statistics = entityManager.createQuery("SELECT s FROM UserStatisticsModel s " +
                                "WHERE s.id.userID = :userID AND s.id.statisticsID = :statisticsID", UserStatisticsModel.class)
                        .setParameter("userID", identity.getUserID())
                        .setParameter("statisticsID", Quota.USER_STORAGE_USED.id())
                        .getSingleResult();
                var storageUsed = Long.valueOf(statistics.getValue());
                var storageToClean = storageUsed - fileJournal.getSize();
                statistics.setValue(String.valueOf(storageToClean));
                entityManager.persist(statistics);
            }
        }
        // update all file versions
        fileJournalRepo.updateStateAndNameByPath(updatedName, updatedState, wsID, fileJournal.getPath());
        // return new name
        return isFileDirectory(fileJournal.getPath()) ? newName
                : StringUtils.substringAfterLast(fileJournal.getPath(), "/");
    }

    public LinkValidity verifyLink(String bareLink) {
        String decryptedLink = "";
        try {
            decryptedLink = linkEncoder.decrypt(bareLink);
        } catch (CryptoException e) {
            log.error("Could not decrypt link " + bareLink, e);
            return new LinkValidity(false, false);
        }

        var link = entityManager.find(LinkModel.class, decryptedLink);
        if (link != null) {
            return new LinkValidity(true, link.getExpiresAt().isBefore(LocalDateTime.now()));
        }
        return new LinkValidity(false, false);
    }

    private void verifyQuotaUsage(UUID userID, Short subscriptionID, Long fileSize) {
        var statistics = entityManager.createQuery("SELECT s FROM UserStatisticsModel s " +
                        "WHERE s.id.userID = :userID AND s.id.statisticsID = :statisticsID", UserStatisticsModel.class)
                .setParameter("userID", userID)
                .setParameter("statisticsID", Quota.USER_STORAGE_USED.id())
                .getSingleResult();

        var storageUsed = Long.valueOf(statistics.getValue());
        var quota = subscriptionQuotaCache.getByID(subscriptionID);
        Long storageLimit;
        if (quota != null) {
            storageLimit = quota.storageLimit();
        } else {
            var subscription = entityManager.find(UserSubscriptionModel.class, subscriptionID);
            storageLimit = subscription.getStorageLimit();
        }
        var remainingQuota = storageLimit - (storageUsed + fileSize);
        if (remainingQuota < 0) {
            throw new InsufficientQuotaException(Quota.USER_STORAGE_USED, String.valueOf(storageLimit));
        }
        log.debug("Verified quota, remaining: " + remainingQuota);
    }

    private Long calculateFileSize(Integer blockCount, Integer lastBlockSize) {
        if (blockCount == 1) {
            return lastBlockSize.longValue();
        } else {
            return BLOCK_SIZE * (blockCount-1) + lastBlockSize.longValue();
        }
    }

    private String createBlockList(Stream<String> hashes) {
        return hashes.collect(Collectors.joining(","));
    }

    private List<String> getBlocksFromBlockList(String blocklist) {
        return Arrays.stream(blocklist.split(",")).collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isFileDirectory(String path) {
        return path.charAt(path.length()-1) == DIRECTORY_CHAR;
    }
}
