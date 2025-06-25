package sanity.nil.block.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.StatusRuntimeException;
import io.minio.PutObjectArgs;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import sanity.nil.block.consts.BlockStatus;
import sanity.nil.block.consts.TaskStatus;
import sanity.nil.block.consts.TaskType;
import sanity.nil.block.db.tables.records.BlocksRecord;
import sanity.nil.block.db.tables.records.TasksRecord;
import sanity.nil.block.dto.BlockUpload;
import sanity.nil.block.dto.BlockUploadRequest;
import sanity.nil.grpc.meta.GetFileBlockListRequest;
import sanity.nil.grpc.meta.MutinyMetadataServiceGrpc;
import sanity.nil.grpc.meta.VerifyLinkRequest;
import sanity.nil.minio.MinioOperations;
import sanity.nil.security.Role;
import sanity.nil.security.WorkspaceIdentityProvider;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static sanity.nil.block.db.tables.Blocks.BLOCKS;

@JBossLog
@ApplicationScoped
public class BlockService {

    @Inject
    DSLContext context;
//    @Inject
//    EntityManager entityManager;
    @Inject
    MinioOperations minio;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    WorkspaceIdentityProvider identityProvider;
    @GrpcClient("metadataService")
    MutinyMetadataServiceGrpc.MutinyMetadataServiceStub metadataClient;
    @Inject
    UserTransaction userTransaction;
    private final Integer BATCH_SIZE = 50;

    @Transactional
    public BlockUpload saveBlocks(List<FileUpload> files, BlockUploadRequest request) {
        var identity = identityProvider.getIdentity();
        if (!identity.hasRole(Role.USER)) {
            throw new UnauthorizedException();
        }
        log.info(String.format("Correlation-ID: %s for req: \n%s", request.correlationID(), request));
        var results = new ArrayList<BlockUpload.UploadResult>();
        for (FileUpload block : files) {
            try {
                try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(block.filePath().toFile()))) {
                    minio.putObject(PutObjectArgs.builder()
                            .stream(inputStream, block.size(), -1)
                            .object(block.fileName())
                    );
                }
            } catch (Exception e) {
                log.error(e.toString());
                throw new RuntimeException(e);
            }

            var newBlock = new BlocksRecord(getCurrDate(), block.fileName(), BlockStatus.AWAITING_UPLOAD.name());
            newBlock.insert();
//            entityManager.persist(newBlock);
            results.add(new BlockUpload.UploadResult(block.fileName(), true));
        }
        return new BlockUpload(request.correlationID(), results);
    }


    public Set<String> findExistingHashes(List<String> hashes) {
        ExecutorService executor = Executors.newFixedThreadPool(hashes.size()/BATCH_SIZE);

        List<List<String>> batches = IntStream.range(0, (hashes.size() + BATCH_SIZE - 1) / BATCH_SIZE)
                .mapToObj(i -> hashes.subList(i * BATCH_SIZE, Math.min(hashes.size(), (i + 1) * BATCH_SIZE)))
                .collect(Collectors.toList());

        List<CompletableFuture<List<String>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> queryBatch(batch), executor))
                .collect(Collectors.toList());

        Set<String> foundHashes = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        executor.shutdown();
        return foundHashes;
    }

    private List<String> queryBatch(List<String> batch) {
        List<String> res = new ArrayList<>();
        try {
            userTransaction.begin();
//            String query = "SELECT b.hash FROM BlockModel b WHERE b.hash IN :hashes";
            res = context.select(BLOCKS.HASH).where(BLOCKS.HASH.in(batch))
                    .fetch(BLOCKS.HASH);
//            res = entityManager.createQuery(query, String.class)
//                    .setParameter("hashes", batch)
//                    .getResultList();
            userTransaction.commit();
        } catch (Exception e) {
            log.error(e.toString());
        }
        return res;
    }

    public List<String> checkBlocksExistence(List<String> hashList) {
        log.info("BlockService thread: " + Thread.currentThread().getName());

        Collection<String> existingBlocks = new ArrayList<>();
        if (hashList.size() < BATCH_SIZE) {
            existingBlocks = context.select(BLOCKS.HASH).where(BLOCKS.HASH.in(hashList))
                    .fetch(BLOCKS.HASH);
//            existingBlocks = entityManager.createQuery("SELECT u.hash FROM BlockModel u WHERE u.hash in ?1 ", String.class)
//                    .setParameter(1, hashList)
//                    .getResultList();
        } else {
            existingBlocks = findExistingHashes(hashList);
        }

        if (existingBlocks.isEmpty()) {
            return hashList;
        } else {
            var existingBlockSet = existingBlocks.stream()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            // check and filter out already existing hashes, maintaining position of new ones to insert
            return hashList.stream()
                    .filter(hash -> !existingBlockSet.contains(hash))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    @Transactional
    public boolean deleteBlocks(List<String> hashList) {
        var metadata = objectMapper.createObjectNode();
        metadata.put("blocksToDelete", String.join(",", hashList));
        var delayedTask = new TasksRecord(null, (short) 0, getCurrDate(), TaskType.DELETE_BLOCKS.name(), null,
                TaskStatus.CREATED.name(), JSONB.valueOf(metadata.toString()));
        delayedTask.insert();
//        var delayedTask = new TaskModel(null, TaskType.DELETE_BLOCKS, metadata, TaskStatus.CREATED);
//        entityManager.persist(delayedTask);
        return true;
    }

    public Multi<byte[]> getFileBlock(String fileID, String wsID, String link) {
        return hasPermissions(wsID, link)
                .flatMap(permission -> {
                    if (!permission) {
                        return Uni.createFrom().failure(new ForbiddenException());
                    }
                    return metadataClient.getFileBlockList(GetFileBlockListRequest.newBuilder()
                                    .setFileID(fileID).setWsID(wsID).build())
                            .ifNoItem().after(Duration.ofSeconds(2)).failWith(NoResultException::new)
                            .onFailure(StatusRuntimeException.class).transform(failure -> new NotFoundException());
                })
                .onItem().transformToMulti(blockListResponse ->
                        Multi.createFrom().iterable(blockListResponse.getBlockList())
                                .onItem().transformToMulti(this::getBlock)
                                .concatenate()
                );
    }

    public Multi<byte[]> getBlock(String hash)  {
        try (InputStream minioStream = minio.getObject(hash)) {
            return Multi.createFrom().item(minioStream.readAllBytes());
        } catch (Exception e) {
            throw new WebApplicationException("Error downloading file: " + hash, e);
        }
    }

    private Uni<Boolean> hasPermissions(String wsID, String link) {
        if (StringUtils.isNotEmpty(link)) {
            return metadataClient.verifyLink(VerifyLinkRequest.newBuilder().setLink(link).build())
                    .onItem().transform(response -> response.getValid() && !response.getExpired());
        } else {
            return identityProvider.getIdentityUni(wsID)
                    .onItem().transform(identity -> identity.hasRole(Role.USER));
        }
    }

    private OffsetDateTime getCurrDate() {
        return LocalDateTime.now().atZone(ZoneId.of("UTC")).toOffsetDateTime();
    }

}
