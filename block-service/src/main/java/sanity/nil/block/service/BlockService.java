package sanity.nil.block.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import sanity.nil.block.consts.BlockStatus;
import sanity.nil.block.consts.TaskStatus;
import sanity.nil.block.consts.TaskType;
import sanity.nil.block.dto.BlockUpload;
import sanity.nil.block.dto.BlockUploadRequest;
import sanity.nil.block.infra.minio.MinioOperations;
import sanity.nil.block.model.BlockModel;
import sanity.nil.block.model.TaskModel;
import sanity.nil.grpc.meta.GetFileBlockListRequest;
import sanity.nil.grpc.meta.MutinyMetadataServiceGrpc;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JBossLog
@ApplicationScoped
public class BlockService {

    @Inject
    EntityManager entityManager;
    @Inject
    MinioOperations minio;
    @Inject
    ObjectMapper objectMapper;
    @GrpcClient("metadataService")
    MutinyMetadataServiceGrpc.MutinyMetadataServiceStub metadataClient;

    @Transactional
    public BlockUpload saveBlocks(List<FileUpload> files, BlockUploadRequest request) {
        log.info(String.format("Correlation-ID: %s for req: \n%s", request.correlationID(), request));
        String bucket = "test";
        var results = new ArrayList<BlockUpload.UploadResult>();
        for (FileUpload block : files) {
            try {
                minio.createBucketIfNotExists(bucket);
                try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(block.filePath().toFile()))) {
                    minio.putObject(PutObjectArgs.builder()
                            .stream(inputStream, block.size(), -1)
                            .object(block.fileName())
                            .bucket(bucket)
                            .build());
                }
            } catch (Exception e) {
                log.error(e.toString());
                throw new RuntimeException(e);
            }

            var newBlock = new BlockModel(block.fileName(), BlockStatus.AWAITING_UPLOAD, ZonedDateTime.now());
            entityManager.persist(newBlock);
            results.add(new BlockUpload.UploadResult(block.fileName(), true));
        }
//        client.blockUploadedEvent(new BlocksUploadedEvent(String.valueOf(request.correlationID()),
//                files.stream().map(file -> new BlocksUploadedEvent.UploadedBlock(file.fileName(), file.size())).toList()));
        return new BlockUpload(request.correlationID(), results);
    }

    public List<String> checkBlocksExistence(List<String> hashList) {
        log.info("BlockService thread: " + Thread.currentThread().getName());

        var existingBlocks = entityManager.createQuery("SELECT u FROM BlockModel u WHERE u.hash in ?1 ", BlockModel.class)
                .setParameter(1, hashList)
                .getResultList();

        if (existingBlocks.isEmpty()) {
            return hashList;
        } else {
            var existingBlockSet = existingBlocks.stream()
                    .map(BlockModel::getHash)
                    .collect(Collectors.toSet());
            // check and filter out already existing hashes, maintaining position of new ones to insert
            var missingBlocks = hashList.stream()
                    .filter(hash -> !existingBlockSet.contains(hash))
                    .toList();

            return missingBlocks;
        }
    }

    @Transactional
    public boolean deleteBlocks(List<String> hashList) {
        var metadata = objectMapper.createObjectNode();
        metadata.put("blocksToDelete", String.join(",", hashList));
        var delayedTask = new TaskModel(null, TaskType.DELETE_BLOCKS, metadata, TaskStatus.CREATED);
        entityManager.persist(delayedTask);
        return true;
    }

//    public Multi<byte[]> getFileBlock(String fileID) {
//
//        var blocks = metadataClient.getFileBlockList(GetFileBlockListRequest.newBuilder()
//                .setId(fileID).build()
//        );
//        if (blocks != null) {
//            log.info("Metadata response: " + blocks.getBlockList().size());
//
//            // For each block, get its stream and flatten
//            return Multi.createFrom().iterable(blocks.getBlockList())
//                    .onItem().transformToMulti(hash -> getBlock(hash))
//                    .concatenate(); // Maintain block order
//        }
//        return Multi.createFrom().empty();
//    }

    public Multi<byte[]> getFileBlock(String fileID) {

        return metadataClient.getFileBlockList(GetFileBlockListRequest.newBuilder()
                        .setId(fileID).build())
                .ifNoItem().after(Duration.ofSeconds(2)).failWith(NoResultException::new)
                .onItem().transformToMulti(blockListResponse -> {
                    return Multi.createFrom().iterable(blockListResponse.getBlockList())
                            .onItem().transformToMulti(hash -> getBlock(hash))
                            .concatenate();
                });
    }

    public Multi<byte[]> getBlock(String hash)  {
        try (InputStream minioStream = minio.getMinioClient().getObject(
                GetObjectArgs.builder().bucket("test").object(hash).build())) {
            return Multi.createFrom().item(minioStream.readAllBytes());
        } catch (Exception e) {
            throw new WebApplicationException("Error downloading file: " + hash, e);
        }
    }

}
