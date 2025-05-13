package sanity.nil.meta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.faulttolerance.Retry;
import sanity.nil.grpc.block.BlockService;
import sanity.nil.grpc.block.CheckBlocksExistenceRequest;
import sanity.nil.grpc.block.Code;
import sanity.nil.grpc.block.DeleteBlocksRequest;
import sanity.nil.meta.cache.FileMetadataCache;
import sanity.nil.meta.cache.model.FileMetadata;
import sanity.nil.meta.consts.*;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlocksMetadata;
import sanity.nil.meta.dto.file.FileFilters;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.dto.file.LinkValidity;
import sanity.nil.meta.exceptions.CryptoException;
import sanity.nil.meta.exceptions.InsufficientQuotaException;
import sanity.nil.meta.cache.SubscriptionQuotaCache;
import sanity.nil.meta.model.*;
import sanity.nil.meta.security.LinkEncoder;
import sanity.nil.security.Role;
import sanity.nil.security.WorkspaceIdentityProvider;
import sanity.nil.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sanity.nil.meta.consts.Constants.BLOCK_SIZE;
import static sanity.nil.meta.consts.Constants.FILE_VERSIONS_MAX;

@JBossLog
@ApplicationScoped
public class MetadataService {

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

        entityManager.createQuery("UPDATE FileJournalModel f SET f.state = :newState " +
                        "WHERE f.id.fileID = :fileID AND f.id.workspaceID = :wsID")
                .setParameter("newState", FileState.DELETED)
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

        List<String> missingBlocks = new ArrayList<>();

        var cachedMetadata = fileMetadataCache.getByParams(request.workspaceID(), null, request.path(), 1);
        if (CollectionUtils.isNotEmpty(cachedMetadata)) {
            var existingBlocks = cachedMetadata.getFirst().blocks();
            var index = 0;
            for (String block : requestSortedBlocks) {
                if (index >= existingBlocks.size()) {
                    missingBlocks.add(block);
                } else if (!existingBlocks.get(index).equals(block)) {
                    missingBlocks.add(block);
                }
                index++;
            }
        } else {
            try {
                var response = blockClient.checkBlocksExistence(CheckBlocksExistenceRequest.newBuilder()
                        .addAllHash(requestSortedBlocks).build()
                ).await().atMost(Duration.ofSeconds(2L));
                missingBlocks = response.getMissingBlocksList();
            } catch (Exception e) {
                log.error("Error checking blocks existence", e);
            }
        }

        if (missingBlocks.isEmpty()) {
            log.debug("All blocks already exist");
            var existingFile = updateFileState(request.workspaceID(), request.path());
            // TODO: send a request to block service to update block states
            var cacheEntry = new FileMetadata(existingFile.getPath(), getBlocksFromBlockList(existingFile.getBlocklist()),
                    existingFile.getSize(), existingFile.getState());
            fileMetadataCache.persistFileMetadata(cacheEntry, request.workspaceID(), existingFile.getFileID());
            return new BlockMetadata(request.correlationID());
        }
        var fileSize = calculateFileSize(missingBlocks.size(), request.lastBlockSize());
        verifyQuotaUsage(identity.getUserID(), userUploader.getSubscription().getId(), fileSize);

        var workspace = getWorkspace(request.workspaceID(), identity.getUserID()).get();
        if (missingBlocks.size() == requestSortedBlocks.size()) {
            postProcessNewFile(request.path(), userUploader, missingBlocks, workspace, fileSize);
        } else {
            postProcessUpdateFile(request.path(), userUploader, requestSortedBlocks, workspace, fileSize);
        }

        return new BlockMetadata(request.correlationID(), missingBlocks);
    }

    @Transactional
    protected void postProcessNewFile(String path, UserModel userUploader, List<String> newBlocks, WorkspaceModel workspace, Long fileSize) {
        FileJournalModel fileJournal = new FileJournalModel(entityManager.merge(workspace), path, userUploader, FileState.IN_UPLOAD,
                fileSize, createBlockList(newBlocks.stream()), 0);
        fileJournalRepo.insert(fileJournal);
        var userStatistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
                identityProvider.getCheckedIdentity().getUserID(), Quota.USER_STORAGE_USED.id())
        );
        userStatistics.setValue(String.valueOf(
                Long.parseLong(userStatistics.getValue()) + fileSize)
        );
        entityManager.persist(userStatistics);
    }

    @Transactional
    protected void postProcessUpdateFile(String path, UserModel userUploader, Set<String> newBlocks, WorkspaceModel workspace, Long fileSize) {
        var existingVersions = entityManager.createQuery("SELECT f.historyID FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND f.path = :path " +
                        "AND f.state <> :state", Integer.class)
                .setParameter("wsID", workspace.getId())
                .setParameter("path", path)
                .setParameter("state", FileState.DELETED)
                .getResultList();

        if (existingVersions.size() == FILE_VERSIONS_MAX) {
            // TODO: delete blocks for file versions that are cleaned up
            // or to maintain a policy of soft deletes?
            entityManager.createQuery("DELETE FROM FileJournalModel fj " +
                            "WHERE fj.historyID = :version")
                    .setParameter("version", existingVersions.stream().sorted().findFirst().get())
                    .executeUpdate();
        }

        var fileJournal = entityManager.createQuery("SELECT fj FROM FileJournalModel fj " +
                        "WHERE fj.id.workspaceID = ?1 AND fj.path = ?2 " +
                        "ORDER BY fj.historyID DESC", FileJournalModel.class)
                .setParameter(1, workspace.getId())
                .setParameter(2, path)
                .setMaxResults(1)
                .getSingleResult();

        var newFileJournalEntry = new FileJournalModel(entityManager.merge(workspace), path, userUploader,
                FileState.IN_UPLOAD, fileJournal.getSize() + fileSize, createBlockList(newBlocks.stream()),fileJournal.getHistoryID()+1
        );
        fileJournalRepo.insert(newFileJournalEntry);

        log.debug("FileJournal entry history updated to: " + fileJournal.getHistoryID());
        var userStatistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
                identityProvider.getCheckedIdentity().getUserID(), Quota.USER_STORAGE_USED.id())
        );
        userStatistics.setValue(String.valueOf(
                Long.parseLong(userStatistics.getValue()) + fileSize)
        );
        entityManager.persist(userStatistics);
    }

    @Transactional
    protected FileJournalModel updateFileState(Long workspaceID, String path) {
        var uploadedFile = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND f.path = :path " +
                        "ORDER BY f.historyID DESC " +
                        "FETCH FIRST 1 ROW ONLY ", FileJournalModel.class)
                .setParameter("wsID", workspaceID)
                .setParameter("path", path)
                .getSingleResult();

        if (uploadedFile.getState().equals(FileState.IN_UPLOAD)) {
            uploadedFile.setState(FileState.UPLOADED);
            entityManager.merge(uploadedFile);
            log.debug("File was uploaded before, now updated state");
        }
        return uploadedFile;
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

    public FileInfo getFileInfo(String fileID, String wsID, String link) {
        if (!hasFileAccessPermissions(wsID, link)) {
            throw new ForbiddenException();
        }
        if (!StringUtils.isNumeric(fileID)) {
            return null;
        }
        // TODO: optimize reads by not selecting not needed columns (blocklist)
        var fileOp = fileJournalRepo.findByIdAndStateIn(Long.valueOf(fileID), Long.valueOf(wsID), FileState.UPLOADED);
        if (fileOp.isEmpty()) {
            throw new NotFoundException();
        }
        var file = fileOp.get();

        return new FileInfo(file.getFileID(), file.getId().getWorkspaceID(), file.getPath(),
                file.getSize(), file.getUploader().getId(), file.getCreatedAt());
    }

    public Paged<FileInfo> searchFiles(FileFilters filters) {
        var identity = identityProvider.getIdentity(String.valueOf(filters.wsID()));
        if (!identity.hasRole(Role.USER)) {
            throw new ForbiddenException();
        }
        var page = filters.page() == null ? 0 : filters.page();
        var size = filters.size() == null ? 10 : filters.size();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FileJournalModel> selectQuery = cb.createQuery(FileJournalModel.class);
        Root<FileJournalModel> selectRoot = selectQuery.from(FileJournalModel.class);

        var selectPredicates = buildPredicates(cb, selectRoot, filters);

        selectQuery.select(selectRoot).where(cb.and(selectPredicates.toArray(new Predicate[0])));

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FileJournalModel> countRoot = countQuery.from(FileJournalModel.class);
        var countPredicates = buildPredicates(cb, countRoot, filters);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        var filesFound = entityManager.createQuery(selectQuery).setFirstResult(page*size).setMaxResults(size).getResultList();
        var filesCount = entityManager.createQuery(countQuery).getSingleResult();

        List<FileInfo> dtos = filesFound.stream()
                .map(f -> new FileInfo(
                        f.getFileID(),
                        f.getId().getWorkspaceID(),
                        f.getPath(),
                        f.getSize(),
                        f.getUploader().getId(),
                        f.getCreatedAt()
                )).toList();

        int totalPages = (int) Math.ceil((double) filesCount / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        return new Paged<FileInfo>().of(dtos, totalPages, hasNext, hasPrevious);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<FileJournalModel> root, FileFilters filters) {
        List<Predicate> predicates = new ArrayList<>();
        if (filters.wsID() != null) {
            predicates.add(cb.equal(root.get("id").get("workspaceID"), filters.wsID()));
        }
        if (StringUtils.isNotEmpty(filters.name())) {
            predicates.add(cb.like(cb.upper(root.get("path")), "%" + filters.name().toUpperCase() + "%"));
        }
        if (filters.deleted() != null && filters.deleted()) {
            predicates.add(cb.equal(root.get("state"), FileState.DELETED));
        } else {
            predicates.add(cb.equal(root.get("state"), FileState.UPLOADED));
        }
        if (filters.userID() != null) {
            predicates.add(cb.equal(root.get("uploader").get("id"), filters.userID()));
        }
        return predicates;
    }

    private boolean hasFileAccessPermissions(String wsID, String link) {
        if (StringUtils.isNotEmpty(link)) {
            var verifiedLink = verifyLink(link);
            return verifiedLink.valid() && !verifiedLink.expired();
        } else {
            var identity = identityProvider.getIdentity(wsID);
            return identity.hasRole(Role.USER);
        }
    }

    public boolean existsUserWorkspace(UUID userID, String workspaceID) {
        return getWorkspace(Long.valueOf(workspaceID), userID).isPresent();
    }

    @Transactional
    public String constructLinkForSharing(String fileID, String wsID, TimeUnit timeUnit, Long expiresIn) {
        // TODO: include device identification (user-agent or fingerprint) in link construction
        var identity = identityProvider.getIdentity(wsID);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(expiresIn, timeUnit.toTemporalUnit());
        long diff = expiresAt.toInstant(ZoneOffset.UTC).toEpochMilli() - now.toInstant(ZoneOffset.UTC).toEpochMilli();
        String link = String.format("%s:%s:%s:%s", identity.getUserID(), fileID, wsID, diff);

        String encryptedLink = "";
        try {
            encryptedLink = linkEncoder.encrypt(link);
        } catch (CryptoException e) {
            log.error("Could not encrypt link " + link, e);
            return "";
        }
        LinkModel linkModel = new LinkModel(identity.getUserID(), link, expiresAt);
        entityManager.persist(linkModel);

        return encryptedLink;
    }

    @Transactional
    public String renameFile(String fileID, Long wsID, String newName) {
        var identity = identityProvider.getIdentity(String.valueOf(wsID));

        var fileJournalOp = fileJournalRepo.findById(Long.valueOf(fileID), wsID);
        if (fileJournalOp.isEmpty()) {
            throw new NotFoundException();
        }
        var fileJournal = fileJournalOp.get();
        if (!fileJournal.getUploader().getId().equals(identity.getUserID())) {
            throw new UnauthorizedException("File wasn't uploaded by current user");
        }

        fileJournal.setPath(newName);
        entityManager.persist(fileJournal);
        return newName;
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
        return Arrays.stream(blocklist.split(",")).collect(Collectors.toList());
    }
}
