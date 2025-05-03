package sanity.nil.meta.service;

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
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.faulttolerance.Retry;
import sanity.nil.grpc.block.BlockService;
import sanity.nil.grpc.block.CheckBlocksExistenceRequest;
import sanity.nil.grpc.block.Code;
import sanity.nil.grpc.block.DeleteBlocksRequest;
import sanity.nil.meta.consts.Quota;
import sanity.nil.meta.consts.TimeUnit;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlocksMetadata;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.dto.file.LinkValidity;
import sanity.nil.meta.exceptions.CryptoException;
import sanity.nil.meta.exceptions.InsufficientQuotaException;
import sanity.nil.meta.infra.cache.SubscriptionQuotaCache;
import sanity.nil.meta.model.*;
import sanity.nil.meta.security.LinkEncoder;
import sanity.nil.security.Role;
import sanity.nil.security.WorkspaceIdentityProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JBossLog
@ApplicationScoped
public class MetadataService {

    @Inject
    EntityManager entityManager;
    @Inject
    WorkspaceIdentityProvider identityProvider;
    @Inject
    SubscriptionQuotaCache subscriptionQuotaCache;
    @Inject
    LinkEncoder linkEncoder;
    @GrpcClient("blockService")
    BlockService blockClient;

    private final static Long BLOCK_SIZE = (long) (4 * 1024 * 1024);
    private final static Integer FILE_VERSIONS_MAX = 3;

    public List<String> getFileBlockList(String fileID, String workspaceID, Integer versions) {
        log.info("MetadataService thread: " + Thread.currentThread().getName());
        var fileJournal = entityManager.createQuery("SELECT f.blocklist FROM FileJournalModel f " +
                        "WHERE f.id.fileID = :fileID AND f.id.workspaceID = :wsID " +
                        "ORDER BY f.id.version DESC " +
                        "FETCH FIRST :versions ONLY ", FileJournalModel.class)
                .setParameter("fileID", fileID)
                .setParameter("wsID", workspaceID)
                .setParameter("versions", versions)
                .getSingleResult();

        if (fileJournal == null) {
            throw new NoSuchElementException();
        }
        return getBlocksFromBlockList(fileJournal.getBlocklist());
    }

    public void deleteFile(String fileIDParam, String workspaceIDParam) {
        var identity = identityProvider.getIdentity(workspaceIDParam);
        if (!identity.hasRole(Role.USER)) {
            throw new UnauthorizedException();
        }

        Long fileID = Long.valueOf(fileIDParam);
        Long workspaceID = Long.valueOf(workspaceIDParam);
        // get all file versions blocks to delete
        var blockList = getFileBlockList(fileIDParam, workspaceIDParam, FILE_VERSIONS_MAX);

        deleteFileJournal(fileID, workspaceID);
        deleteBlocksWithRetry(blockList);
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

    @Transactional
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
        var fileSize = calculateFileSize(request.blocks().size(), request.lastBlockSize());
        verifyQuotaUsage(identity.getUserID(), fileSize);

        var userUploader = entityManager.find(UserModel.class, identity.getUserID());

        log.debug("MetadataService thread: " + Thread.currentThread().getName());

        var requestBlocks = request.blocks().stream()
                .sorted(Comparator.comparing(a -> a.position()))
                .map(e -> e.hash())
                .collect(Collectors.toCollection(LinkedHashSet::new));;

        List<String> missingBlocks = new ArrayList<>();
        try {
            var response = blockClient.checkBlocksExistence(CheckBlocksExistenceRequest.newBuilder()
                    .addAllHash(requestBlocks).build()
            ).await().atMost(Duration.ofSeconds(2L));
            missingBlocks = response.getMissingBlocksList();
        } catch (Exception e) {
            log.error("Error checking blocks existence", e);
        }

        if (missingBlocks.isEmpty()) {
            log.debug("All blocks already exist");
            // TODO: check if file is in state uploaded, if not, update it so it is
            return new BlockMetadata(request.correlationID());
        }

        var workspace = getWorkspace(request.workspaceID(), identity.getUserID()).get();
        if (missingBlocks.size() == requestBlocks.size()) {
            postProcessNewFile(request.filename(), userUploader, missingBlocks, workspace, fileSize);
        } else {
            var rawFilename = StringUtils.substringBeforeLast(request.filename(), ".");
            postProcessUpdateFile(rawFilename, userUploader, requestBlocks, workspace, fileSize);
        }

        return new BlockMetadata(request.correlationID(), missingBlocks);
    }

    @Transactional
    protected void postProcessNewFile(String filename, UserModel userUploader, List<String> newBlocks, WorkspaceModel workspace, Long fileSize) {
        var rawFilename = StringUtils.substringBeforeLast(filename, ".");
        var contentType = StringUtils.substringAfterLast(filename, ".");
        FileModel newFile = new FileModel(userUploader, rawFilename, contentType, fileSize);
        entityManager.persist(newFile);
        FileJournalModel fileJournal = new FileJournalModel(workspace, newFile, createBlockList(newBlocks.stream()),0, 0);
        entityManager.merge(fileJournal);
        var userStatistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
                identityProvider.getCheckedIdentity().getUserID(), Quota.USER_STORAGE_USED.id())
        );
        userStatistics.setValue(String.valueOf(
                Long.parseLong(userStatistics.getValue()) + fileSize)
        );
        entityManager.persist(userStatistics);
    }

    @Transactional
    protected void postProcessUpdateFile(String filename, UserModel userUploader, Set<String> newBlocks, WorkspaceModel workspace, Long fileSize) {
        var existingVersions = entityManager.createQuery("SELECT f.id.version FROM FileJournalModel f " +
                    "WHERE f.id.workspaceID = ?1 AND f.file.filename = ?2", Integer.class)
                .setParameter(1, workspace.getId())
                .setParameter(2, filename)
                .getResultList();

        if (existingVersions.size() == FILE_VERSIONS_MAX) {
            // TODO: delete blocks for file versions that are cleaned up
            entityManager.createQuery("DELETE FROM FileJournalModel f " +
                            "WHERE f.id.version = :version")
                    .setParameter("version", existingVersions.stream().sorted().findFirst().get())
                    .executeUpdate();
        }

        var fileJournal = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = ?1 AND f.file.filename = ?2 " +
                        "ORDER BY f.id.version DESC " +
                        "FETCH FIRST 1 ROW ONLY", FileJournalModel.class)
                .setParameter(1, workspace.getId())
                .setParameter(2, filename)
                .getSingleResult();

        var file = entityManager.find(FileModel.class, fileJournal.getId().fileID());
        file.setSize(fileSize);
        file.setUploader(userUploader);
        entityManager.merge(file);

        var newFileJournalEntry = new FileJournalModel(entityManager.merge(workspace), entityManager.merge(file), createBlockList(newBlocks.stream()),
                fileJournal.getHistoryID()+1, fileJournal.getId().version()+1
        );
        entityManager.persist(newFileJournalEntry);
        log.debug("FileJournal entry history updated to: " + fileJournal.getHistoryID());
        var userStatistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
                identityProvider.getCheckedIdentity().getUserID(), Quota.USER_STORAGE_USED.id())
        );
        userStatistics.setValue(String.valueOf(
                Long.parseLong(userStatistics.getValue()) + fileSize)
        );
        entityManager.persist(userStatistics);
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
        var file = entityManager.find(FileModel.class, Long.valueOf(fileID));

        return new FileInfo(file.getFilename(), file.getContentType(), file.getCreatedAt());
    }

    public Paged<FileInfo> searchFiles(Long wsID, UUID userID, int page, int size) {
        var identity = identityProvider.getIdentity(String.valueOf(wsID));
        if (!identity.hasRole(Role.USER)) {
            throw new ForbiddenException();
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FileJournalModel> selectQuery = cb.createQuery(FileJournalModel.class);
        Root<FileJournalModel> selectRoot = selectQuery.from(FileJournalModel.class);

        var selectPredicates = buildPredicates(cb, selectRoot, wsID, userID);

        selectQuery.select(selectRoot).where(cb.and(selectPredicates.toArray(new Predicate[0])));

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FileJournalModel> countRoot = countQuery.from(FileJournalModel.class);
        var countPredicates = buildPredicates(cb, countRoot, wsID, userID);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        var filesFound = entityManager.createQuery(selectQuery).getResultList();
        var filesCount = entityManager.createQuery(countQuery).getSingleResult();

        List<FileInfo> dtos = filesFound.stream()
                .map(f -> new FileInfo(
                        f.getFile().getFilename(),
                        f.getFile().getContentType(),
                        f.getFile().getCreatedAt()
                )).toList();

        int totalPages = (int) Math.ceil((double) filesCount / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        return new Paged<FileInfo>().of(dtos, totalPages, hasNext, hasPrevious);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<FileJournalModel> root, Long wsID, UUID userID) {
        List<Predicate> predicates = new ArrayList<>();
        if (wsID != null) {
            predicates.add(cb.equal(root.get("id").get("workspaceID"), wsID));
        }
        if (userID != null) {
            predicates.add(cb.equal(root.get("file").get("uploader").get("id"), userID));
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

    private void verifyQuotaUsage(UUID userID, Long fileSize) {
        var statistics = entityManager.createQuery("SELECT s FROM UserStatisticsModel s " +
                        "WHERE s.id.userID = :userID AND s.id.statisticsID = :statisticsID", UserStatisticsModel.class)
                .setParameter("userID", userID)
                .setParameter("statisticsID", Quota.USER_STORAGE_USED.id())
                .getSingleResult();

        var storageUsed = Long.valueOf(statistics.getValue());
        var quota = subscriptionQuotaCache.getByQuota(Quota.USER_STORAGE_USED);
        var remainingQuota = quota.storageLimit() - (storageUsed + fileSize);
        if (remainingQuota < 0) {
            throw new InsufficientQuotaException(Quota.USER_STORAGE_USED, String.valueOf(quota.storageLimit()));
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
