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
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
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
import sanity.nil.grpc.meta.VerifyLinkRequest;
import sanity.nil.security.Identity;
import sanity.nil.security.Role;
import sanity.nil.security.WorkspaceIdentityProvider;

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
    @Inject
    WorkspaceIdentityProvider identityProvider;
    @GrpcClient("metadataService")
    MutinyMetadataServiceGrpc.MutinyMetadataServiceStub metadataClient;

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

}
