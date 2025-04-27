package sanity.nil.meta.service;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.faulttolerance.Retry;
import sanity.nil.grpc.block.BlockServiceGrpc;
import sanity.nil.grpc.block.CheckBlocksExistenceRequest;
import sanity.nil.grpc.block.Code;
import sanity.nil.grpc.block.DeleteBlocksRequest;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlockMetadata;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.model.FileJournalModel;
import sanity.nil.meta.model.FileModel;
import sanity.nil.meta.model.LinkModel;
import sanity.nil.meta.model.WorkspaceModel;
import sanity.nil.security.Role;
import sanity.nil.security.WorkspaceIdentityProvider;

import java.time.ZonedDateTime;
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
    @GrpcClient("blockService")
    BlockServiceGrpc.BlockServiceBlockingStub blockClient;

    private final static Long BLOCK_SIZE = (long) (4 * 1024 * 1024);

    public List<String> getFileBlockList(String fileID, String workspaceID) {
        log.info("MetadataService thread: " + Thread.currentThread().getName());
        var fileJournal = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE f.id.fileID = :fileID AND f.id.workspaceID = :wsID " +
                        "ORDER BY f.historyID DESC", FileJournalModel.class)
                .setParameter("fileID", fileID)
                .setParameter("wsID", workspaceID)
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
        // TODO: rn taking only blocks from latest version, but what if prev versions had blocks that aren't in latest?
        var blockList = getFileBlockList(fileIDParam, workspaceIDParam);

        deleteFileJournal(fileID);

        deleteBlocksWithRetry(blockList);
    }

    //    @CircuitBreaker()
    @Retry(maxRetries = 3, delay = 100, delayUnit = ChronoUnit.MILLIS)
    public void deleteBlocksWithRetry(List<String> blockList) {

        var response = blockClient.deleteBlocks(DeleteBlocksRequest.newBuilder()
                .addAllHash(blockList).build()
        );
        if (response.getCode().equals(Code.failure)) {
            throw new RuntimeException("Error calling delete blocks");
        }
    }

    @Transactional
    public void deleteFileJournal(Long fileID) {
        var deleted = entityManager.createQuery("DELETE FROM FileJournalModel f WHERE f.id.fileID = :fileID")
                .setParameter("fileID", fileID)
                .executeUpdate();
    }

    public BlockMetadata getBlocksMetadata(GetBlockMetadata request) {
        var identity = identityProvider.getIdentity(String.valueOf(request.workspaceID()));
        if (!identity.hasRole(Role.USER)) {
            throw new UnauthorizedException();
        }

        log.debug("MetadataService thread: " + Thread.currentThread().getName());
        var workspace = getWorkspace(request.workspaceID(), identity.getUserID());
        if (workspace.isEmpty()) {
            return new BlockMetadata(request.correlationID());
        }

        var requestBlocks = request.blocks().stream()
                .sorted(Comparator.comparing(a -> a.position))
                .map(e -> e.hash).collect(Collectors.toSet());

        List<String> missingBlocks = new ArrayList<>();
        try {
            var response = blockClient.checkBlocksExistence(CheckBlocksExistenceRequest.newBuilder()
                    .addAllHash(requestBlocks).build()
            );
            missingBlocks = response.getMissingBlocksList();
        } catch (Exception e) {
            log.error("Error checking blocks existence", e);
        }

        if (missingBlocks.isEmpty()) {
            return new BlockMetadata(request.correlationID());
        }

        postProcessMetadata(request, missingBlocks, requestBlocks, workspace.get());

        return new BlockMetadata(request.correlationID(), missingBlocks);
    }

    @Transactional
    public void postProcessMetadata(GetBlockMetadata request, List<String> missingBlocks, Set<String> requestBlocks, WorkspaceModel workspace) {
        if (missingBlocks.size() == requestBlocks.size()) {
            // TODO: get last block's size from request
            // TODO: check before inserting if user has enough storage to store file, add to statistics inserted file's size

            var filename = StringUtils.substringBeforeLast(request.filename(), ".");
            var contentType = StringUtils.substringAfterLast(request.filename(), ".");
            FileModel newFile = new FileModel(filename, contentType);
            entityManager.persist(newFile);
            FileJournalModel fileJournal = new FileJournalModel(workspace, newFile, createBlockList(requestBlocks.stream()),0, 0);
            entityManager.merge(fileJournal);
        } else {
            // TODO: maybe we also need to know at which position this block already exists?
            // F.e. it exists at pos 2, now we are inserting it at pos 3

            // TODO: allow to maintain at max 3 versions of a file
            var fileJournal = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                            "WHERE f.id.workspaceID = ?1 AND f.file.filename = ?2 ", FileJournalModel.class)
                    .setParameter(1, request.workspaceID())
                    .setParameter(2, request.filename())
                    .getSingleResult();
            // TODO: first find a file with latest version to increment it

            fileJournal.setBlocklist(createBlockList(missingBlocks.stream()));
            fileJournal.setHistoryID(fileJournal.getHistoryID() + 1);
            entityManager.persist(fileJournal);
        }
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
        if (!hasPermissions(wsID, link)) {
            throw new ForbiddenException();
        }
        if (!StringUtils.isNumeric(fileID)) {
            return null;
        }
        var file = entityManager.find(FileModel.class, Long.valueOf(fileID));

        return new FileInfo(file.getFilename(), file.getContentType(), file.getCreatedAt());
    }

    public boolean existsUserWorkspace(UUID userID, String workspaceID) {
        return getWorkspace(Long.valueOf(workspaceID), userID).isPresent();
    }

    public boolean verifyLink(String bareLink) {
        var link = entityManager.find(LinkModel.class, bareLink);
        if (link != null) {
            return link.getExpiresAt().isBefore(ZonedDateTime.now());
        }
        return false;
    }

    private boolean hasPermissions(String wsID, String link) {
        if (!link.isBlank()) {
            return verifyLink(link);
        } else {
            var identity = identityProvider.getIdentity(wsID);
            return identity.hasRole(Role.USER);
        }
    }

//    public String blockUploadedEvent(BlocksUploadedEvent event) {
//        var blocks = event.blocks();
//        var updates = entityManager.createQuery("UPDATE BlockModel SET status = ?1 WHERE hash IN ?2 ")
//                .setParameter(1, BlockStatus.UPLOADED)
//                .setParameter(2, blocks.stream().map(e -> e.hash).toList())
//                .executeUpdate();
//        if (updates != event.blocks().size()) {
//            throw new IllegalArgumentException("Not all uploaded hashes were updated!");
//        }
//        return "";
//    }

    private Long getFileEstimateSize(Long blockCount) {
        if (blockCount == 1) {
            return 0L; // TODO: get from request the last block's size
        } else {
            return BLOCK_SIZE * blockCount-1;
        }
    }

    private String createBlockList(Stream<String> hashes) {
        return hashes.collect(Collectors.joining(","));
    }

    private List<String> getBlocksFromBlockList(String blocklist) {
        return Arrays.asList(blocklist.split(","));
    }
}
