package sanity.nil.meta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import sanity.nil.grpc.block.BlockService;
import sanity.nil.grpc.block.CheckBlocksExistenceRequest;
import sanity.nil.grpc.block.Code;
import sanity.nil.grpc.block.DeleteBlocksRequest;
import sanity.nil.meta.cache.FileMetadataCache;
import sanity.nil.meta.cache.SubscriptionQuotaCache;
import sanity.nil.meta.cache.model.FileMetadata;
import sanity.nil.meta.consts.*;
import sanity.nil.meta.db.tables.records.FileJournalRecord;
import sanity.nil.meta.db.tables.records.LinksRecord;
import sanity.nil.meta.db.tables.records.TasksRecord;
import sanity.nil.meta.db.tables.records.WorkspacesRecord;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlocksMetadata;
import sanity.nil.meta.dto.file.*;
import sanity.nil.meta.exceptions.CryptoException;
import sanity.nil.meta.exceptions.InsufficientQuotaException;
import sanity.nil.meta.exceptions.InvalidParametersException;
import sanity.nil.meta.mappers.FileMapper;
import sanity.nil.meta.model.FileJournalEntity;
import sanity.nil.meta.security.LinkEncoder;
import sanity.nil.security.Identity;
import sanity.nil.security.Role;
import sanity.nil.security.WorkspaceIdentityProvider;
import sanity.nil.util.CollectionUtils;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static sanity.nil.meta.consts.Constants.*;
import static sanity.nil.meta.db.Tables.*;
import static sanity.nil.meta.db.tables.FileJournal.FILE_JOURNAL;
import static sanity.nil.meta.db.tables.UserStatistics.USER_STATISTICS;
import static sanity.nil.meta.db.tables.UserWorkspaces.USER_WORKSPACES;
import static sanity.nil.meta.db.tables.Users.USERS;
import static sanity.nil.meta.db.tables.Workspaces.WORKSPACES;

@JBossLog
@ApplicationScoped
public class MetadataService {

    @Inject
    UserTransaction userTransaction;
    @Inject
    FileJournalRepo fileJournalRepo;
    @Inject
    DSLContext dslContext;
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

    public List<String> getFileBlockList(Long fileID, Long workspaceID, Integer versions) {
        log.info("MetadataService thread: " + Thread.currentThread().getName());
        var blocklist = dslContext.select(FILE_JOURNAL.BLOCKLIST)
                .from(FILE_JOURNAL)
                .where(FILE_JOURNAL.FILE_ID.eq(fileID))
                .and(FILE_JOURNAL.WS_ID.eq(workspaceID))
                .orderBy(FILE_JOURNAL.HISTORY_ID.desc())
                .limit(versions)
                .fetchOne().into(String.class);

        return getBlocksFromBlockList(blocklist);
    }

    @Transactional
    public void deleteFile(Long fileID, Long workspaceID) {
        var identity = identityProvider.getIdentity(String.valueOf(workspaceID));
        if (!identity.hasRole(Role.USER)) {
            throw new UnauthorizedException();
        }

        dslContext.update(FILE_JOURNAL)
                .set(FILE_JOURNAL.STATE, FileState.DELETED.name())
                .set(FILE_JOURNAL.UPDATED_BY, identity.getUserID())
                .set(FILE_JOURNAL.UPDATED_AT, OffsetDateTime.now())
                .where(FILE_JOURNAL.FILE_ID.eq(fileID))
                .and(FILE_JOURNAL.WS_ID.eq(workspaceID))
                .execute();
        var metadata = objectMapper.createObjectNode();
        metadata.put("workspace", workspaceID);
        var performAt = OffsetDateTime.now().plusDays(Constants.FILE_RETENTION_DAYS);
        var task = new TasksRecord(null, TaskType.DELETE_FILE.name(), TaskStatus.CREATED.name(),
                String.valueOf(fileID), JSONB.valueOf(metadata.toString()), OffsetDateTime.now(), performAt
        );
        dslContext.attach(task);
        task.store();
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
        dslContext.deleteFrom(FILE_JOURNAL)
                .where(FILE_JOURNAL.FILE_ID.eq(fileID))
                .and(FILE_JOURNAL.WS_ID.eq(workspaceID))
                .execute();
    }

    public BlockMetadata getBlocksMetadata(GetBlocksMetadata request) {
        var identity = identityProvider.getIdentity(String.valueOf(request.workspaceID()));
        if (!identity.hasRole(Role.USER)) {
            throw new UnauthorizedException();
        }
        var uploaderSubscription = dslContext.select(USERS.SUBSCRIPTION_ID).from(USERS)
                .where(USERS.ID.eq(identity.getUserID()))
                .fetchOne().into(Short.class);

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
                fileMetadataCache.persistFileMetadata(cacheEntry, request.workspaceID(), existingFile.getFileId(), Duration.ofMinutes(10L));

                var fileInfo = fileJournalRepo.journalToFileInfo(existingFile);
                return new BlockMetadata(request.correlationID(), null, fileInfo);
            } else {
                // force insert because file exists in another workspace
                missingBlocks = requestSortedBlocks;
                existsInAnotherWorkspace = true;
            }
        }
        var fileSize = calculateFileSize(missingBlocks.size(), request.lastBlockSize());
        verifyQuotaUsage(identity.getUserID(), uploaderSubscription, fileSize);

//        var workspace = getWorkspace(request.workspaceID(), identity.getUserID()).get();

        var file = new FileJournalRecord();
        if (cachedMetadata != null && cachedMetadata.state().equals(FileState.IN_UPLOAD)) {
            // if file is not uploaded yet (some blocks weren't delivered) - resumable upload
            return new BlockMetadata(request.correlationID(), missingBlocks, null);
        } else if (existsInAnotherWorkspace) {
            file = postProcessExistingFileNewInWorkspace(request.path(), identity.getUserID(), missingBlocks, request.workspaceID(), fileSize);
            missingBlocks = null;
        } else if (missingBlocks.size() == requestSortedBlocks.size()) {
            file = postProcessNewFile(request.path(), identity.getUserID(), missingBlocks, request.workspaceID(), fileSize);
        } else {
            file = postProcessUpdateFile(request.path(), identity.getUserID(), requestSortedBlocks, request.workspaceID(), fileSize);
        }

        var cacheEntry = fileMapper.journalToMetadata(file);
        fileMetadataCache.persistFileMetadata(cacheEntry, request.workspaceID(), file.getFileId(), Duration.ofMinutes(10L));

        return new BlockMetadata(request.correlationID(), missingBlocks, fileJournalRepo.journalToFileInfo(file));
    }

    @Transactional
    protected FileJournalRecord postProcessExistingFileNewInWorkspace(String path, UUID userUploader, Collection<String> newBlocks, Long wsID, Long fileSize) {
        // separate method because not sure how to treat such case, logic can be rethought
        FileJournalEntity fileJournalEntity = new FileJournalEntity(wsID, path, fileSize, FileState.UPLOADED, newBlocks, userUploader);
        var fileJournal = fileJournalRepo.insert(fileJournalEntity);
        updateUserStatistics(userUploader, Quota.USER_STORAGE_USED.id(), String.valueOf(fileSize), StatisticsOperation.ADD);
        return fileJournal;
    }

    @Transactional
    protected FileJournalRecord postProcessNewFile(String path, UUID userUploader, Collection<String> newBlocks, Long wsID, Long fileSize) {
        FileJournalEntity fileJournalEntity = new FileJournalEntity(wsID, path, fileSize, FileState.IN_UPLOAD, newBlocks, userUploader);
        var fileJournal = fileJournalRepo.insert(fileJournalEntity);
        updateUserStatistics(userUploader, Quota.USER_STORAGE_USED.id(), String.valueOf(fileSize), StatisticsOperation.ADD);
        return fileJournal;
    }

    @Transactional
    protected FileJournalRecord postProcessUpdateFile(String path, UUID userUploader, Set<String> newBlocks, Long wsID, Long fileSize) {
        var existingVersions = dslContext.select(FILE_JOURNAL.HISTORY_ID).from(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(wsID)).and(FILE_JOURNAL.PATH.eq(path))
                .and(FILE_JOURNAL.STATE.notEqual(FileState.DELETED.name()))
                .orderBy(FILE_JOURNAL.HISTORY_ID)
                .fetchInto(Integer.class);

        if (existingVersions.size() == FILE_VERSIONS_MAX) {
            // TODO: delete blocks for file versions that are cleaned up
            // or to maintain a policy of soft deletes?
            dslContext.deleteFrom(FILE_JOURNAL)
                    .where(FILE_JOURNAL.WS_ID.eq(wsID))
                    .and(FILE_JOURNAL.HISTORY_ID.eq(existingVersions.getFirst()))
                    .execute();
        }

        // unmark previous version as latest
        dslContext.update(FILE_JOURNAL)
                .set(FILE_JOURNAL.LATEST, (short) 0)
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(FILE_JOURNAL.HISTORY_ID.eq(existingVersions.getLast()))
                .execute();

        var fileJournal = dslContext.selectFrom(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(FILE_JOURNAL.PATH.eq(path))
                .orderBy(FILE_JOURNAL.HISTORY_ID.desc())
                .limit(1).fetchOne().into(FileJournalRecord.class);

        var newFileJournalEntity = new FileJournalEntity(wsID, path, fileJournal.getSize() + fileSize, FileState.IN_UPLOAD, newBlocks, userUploader);
        var newFileJournalEntry = fileJournalRepo.insert(newFileJournalEntity);

        log.debug("FileJournal entry history updated to: " + newFileJournalEntry.getHistoryId());
        updateUserStatistics(userUploader, Quota.USER_STORAGE_USED.id(), String.valueOf(fileSize), StatisticsOperation.ADD);
        return newFileJournalEntry;
    }

    @Transactional
    protected Optional<FileJournalRecord> updateFileState(UUID userID, Long workspaceID, String path) {

        var uploadedFile = dslContext.selectFrom(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(workspaceID))
                .and(FILE_JOURNAL.PATH.eq(path))
                .orderBy(FILE_JOURNAL.HISTORY_ID.desc())
                .limit(1)
                .fetchOne();

        if (uploadedFile == null) {
            log.info("Same file in another workspace identified, proceeding to creating a new entry");
            return Optional.empty();
        }

        if (uploadedFile.getState().equals(FileState.IN_UPLOAD.name())) {
            uploadedFile.setState(FileState.UPLOADED.name());
            uploadedFile.setUpdatedAt(OffsetDateTime.now());
            uploadedFile.setUpdatedBy(userID);
            uploadedFile.store();
            log.debug("File was uploaded before, now updated state");
        }

        return Optional.of(uploadedFile);
    }

    public Optional<WorkspacesRecord> getWorkspace(Long wsID, UUID userID) {
        return dslContext.select(WORKSPACES).from(WORKSPACES)
                .join(USER_WORKSPACES).on(WORKSPACES.ID.eq(USER_WORKSPACES.WS_ID))
                .where(USER_WORKSPACES.WS_ID.eq(wsID))
                .and(USER_WORKSPACES.USER_ID.eq(userID))
                .fetchOptional()
                .map(r -> dslContext.newRecord(WORKSPACES, r));
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

        FileJournalRecord file = null;
        // TODO: optimize reads by not selecting not needed columns (blocklist)

        // find by path
        if (version != null && fileID != null) {
            if (StringUtils.isEmpty(path)) {
                path = fileJournalRepo.findPathByID(wsID, fileID);
            }
            file = fileJournalRepo.findByPathAndVersion(wsID, path, version);
            log.debug("Returning file info for specific version");
        }

        if (StringUtils.isNotEmpty(link)) {
            try {
                var bareLink = linkEncoder.decrypt(link);
                String[] components = bareLink.split(":");
                // TODO check issuer
                var id = Long.parseLong(components[1]);
                var ws = Long.parseLong(components[2]);
                file = fileJournalRepo.findByIdAndStateIn(id, ws, FileState.UPLOADED);
                log.debug("Found by link");
                if (version != null) {
                    file = fileJournalRepo.findByPathAndVersion(ws, file.getPath(), version);
                    log.debug("Found by link and version");
                }
            } catch (CryptoException e) {
                throw new RuntimeException(e);
            }
        }

        // find by file id
        if (fileID != null) {
            file = fileJournalRepo.findByIdAndStateIn(fileID, wsID, FileState.UPLOADED);
        }

        var fileInfo = fileJournalRepo.journalToFileInfo(file);

        if (listVersions != null && listVersions) {
            if (StringUtils.isEmpty(path)) {
                path = fileJournalRepo.findPathByID(file.getWsId(), file.getFileId());
            }
            var versions = fileJournalRepo.getVersionsByPath(file.getWsId(), path);
            var versionedFile = fileMapper.fileInfoToVersionedFile(fileInfo);
            versionedFile.versions = IntStream.range(1, versions.size()+1).mapToObj(v -> {
                return new FileVersion(v, versions.get(v-1));
            }).toList();
            log.debug("Returning file info with versions");
            return versionedFile;
        }
        return fileInfo;
    }

    /**
    * Returns a paginated list of information about files
     **/
    public Paged<FileInfo> searchFiles(FileFilters filters) {
        Identity identity = (filters.wsID() != null)
                ? identityProvider.getIdentity(String.valueOf(filters.wsID()))
                : identityProvider.getCheckedIdentity();

        if (!identity.hasRole(Role.USER)) {
            throw new ForbiddenException();
        }

        int page = (filters.page() == null) ? 0 : filters.page();
        int size = (filters.size() == null ? 10 : filters.size());

        Condition condition = FILE_JOURNAL.LATEST.eq((short) 1)
                .and(FILE_JOURNAL.PATH.isDistinctFrom("/"))
                .and(FILE_JOURNAL.STATE.eq(filters.deleted() == null || !filters.deleted()
                        ? FileState.UPLOADED.name()
                        : FileState.DELETED.name()));

        if (filters.wsID() != null) {
            condition = condition.and(FILE_JOURNAL.WS_ID.eq(filters.wsID()));
        }

        if (filters.name() != null) {
            String pathLike = filters.name().endsWith("/")
                    ? filters.name() + "%"
                    : filters.name();
            condition = condition.and(FILE_JOURNAL.PATH.like(pathLike));
        }

        var countRecord = dslContext.fetchCount(FILE_JOURNAL, condition);
        int totalPages = (int) Math.ceil((double) countRecord / size);

        var res = fileJournalRepo.getFileInfoList(condition, size, page);

        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        return new Paged<FileInfo>().of(res, totalPages, hasNext, hasPrevious);
    }

    public Paged<FileInfo> listDirectory(String directory, Long wsID, Integer page, Integer size) {
        var identity = identityProvider.getIdentity(String.valueOf(wsID));
        if (!identity.hasRole(Role.USER)) {
            throw new ForbiddenException();
        }

        int currentPage = Optional.ofNullable(page).orElse(0);
        int pageSize = Optional.ofNullable(size).orElse(10);

        Condition condition = fileJournalRepo.buildConditionsFromParams(
                FILE_JOURNAL, wsID, directory);

        long totalCount = dslContext.fetchOne(
                dslContext.selectCount()
                        .from(FILE_JOURNAL)
                        .where(condition)
        ).value1();

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasNext = (currentPage + 1) < totalPages;
        boolean hasPrevious = currentPage > 0;

        List<FileInfo> res = fileJournalRepo.getFileInfoList(condition, pageSize,currentPage * pageSize);

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
        var dir = request.path() + request.name() + "/";
        var newDirectoryEntity = new FileJournalEntity(request.workspaceID(), dir, 0L, FileState.UPLOADED, null, identity.getUserID());
        var newDirectory = fileJournalRepo.insert(newDirectoryEntity);

        return fileJournalRepo.journalToFileInfo(newDirectory);
    }

    public List<String> getDirectories(Long wsID, String path) {
        var directoriesInPath = path == null ? "%" : path + "%";
        return dslContext.select(FILE_JOURNAL.PATH).from(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(FILE_JOURNAL.PATH.like(directoriesInPath))
                .and(FILE_JOURNAL.SIZE.eq(0L))
                .fetch().into(String.class);
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
        LinksRecord linksRecord = new LinksRecord(link, identity.getUserID(), 0, OffsetDateTime.now(),
                LocalDateTime.ofEpochSecond(expiresAt, 0, ZoneOffset.UTC).atOffset(ZoneOffset.UTC)
        );
        dslContext.attach(linksRecord);
        linksRecord.store();

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
        if (!fileJournal.getUploaderId().equals(identity.getUserID())) {
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

        if (fileJournal.getState().equals(FileState.DELETED.name())) {
            String fileIdStr = fileID.toString();
            String workspaceIdStr = wsID.toString();

            List<TasksRecord> deletionTasks = dslContext
                    .selectFrom(TASKS)
                    .where(TASKS.OBJECT_ID.eq(fileIdStr)
                            .and(DSL.field("metadata->>workspace", String.class).eq(workspaceIdStr)))
                    .fetchInto(TasksRecord.class);

            Optional<TasksRecord> deletionTaskOpt = deletionTasks.stream().findFirst();

            if (fileAction == FileAction.RESTORE) {
                updatedState = FileState.UPLOADED;
                deletionTaskOpt.ifPresent(task ->
                        dslContext.deleteFrom(TASKS)
                                .where(TASKS.ID.eq(task.getId()))
                                .execute()
                );
            }

            if (fileAction == FileAction.DELETE_FOREVER) {
                updatedState = FileState.DELETING;
                deletionTaskOpt.ifPresent(task ->
                        dslContext.update(TASKS)
                                .set(TASKS.PERFORM_AT, OffsetDateTime.now())
                                .where(TASKS.ID.eq(task.getId()))
                                .execute()
                );

                updateUserStatistics(identity.getUserID(), Quota.USER_STORAGE_USED.id(), String.valueOf(fileJournal.getSize()), StatisticsOperation.SUBTRACT);
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

        var link = dslContext.selectFrom(LINKS)
                .where(LINKS.LINK.eq(decryptedLink))
                .fetchOne();
        if (link != null) {
            return new LinkValidity(true, link.getExpiresAt().isBefore(OffsetDateTime.now(ZoneId.of("GMT"))));
        }
        return new LinkValidity(false, false);
    }

    private void verifyQuotaUsage(UUID userID, Short subscriptionID, Long fileSize) {
        var statisticsValue = dslContext.select(USER_STATISTICS.VALUE).from(USER_STATISTICS)
                .where(USER_STATISTICS.USER_ID.eq(userID))
                .and(USER_STATISTICS.STATISTICS_ID.eq(Quota.USER_STORAGE_USED.id()))
                .fetchOne().into(String.class);
        var storageUsed = Long.valueOf(statisticsValue);
        var quota = subscriptionQuotaCache.getByID(subscriptionID);
        Long storageLimit;
        if (quota != null) {
            storageLimit = quota.storageLimit();
        } else {
            storageLimit = dslContext.select(USER_SUBSCRIPTIONS.STORAGE_LIMIT).from(USER_SUBSCRIPTIONS)
                    .where(USER_SUBSCRIPTIONS.ID.eq(subscriptionID))
                    .fetchOne().into(Long.class);
        }
        var remainingQuota = storageLimit - (storageUsed + fileSize);
        if (remainingQuota < 0) {
            throw new InsufficientQuotaException(Quota.USER_STORAGE_USED, String.valueOf(storageLimit));
        }
        log.debug("Verified quota, remaining: " + remainingQuota);
    }

    private void updateUserStatistics(UUID userID, short id, String addedValue, StatisticsOperation operation) {
        var statisticsValue = dslContext.select(USER_STATISTICS.VALUE).from(USER_STATISTICS)
                .where(USER_STATISTICS.USER_ID.eq(userID))
                .and(USER_STATISTICS.STATISTICS_ID.eq(id))
                .forNoKeyUpdate()
                .fetchOne().into(String.class);
        var newValue = operation == StatisticsOperation.ADD
                ? Long.parseLong(statisticsValue) + Long.parseLong(addedValue)
                : Long.parseLong(statisticsValue) - Long.parseLong(addedValue);
        dslContext.update(USER_STATISTICS)
                .set(USER_STATISTICS.VALUE, String.valueOf(newValue))
                .where(USER_STATISTICS.USER_ID.eq(userID))
                .and(USER_STATISTICS.STATISTICS_ID.eq(id))
                .execute();
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
