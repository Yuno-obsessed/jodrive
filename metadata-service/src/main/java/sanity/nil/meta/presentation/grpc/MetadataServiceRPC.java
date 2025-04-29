package sanity.nil.meta.presentation.grpc;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.grpc.LogInterceptor;
import sanity.nil.grpc.meta.*;
import sanity.nil.meta.service.MetadataService;

import java.security.InvalidParameterException;
import java.util.UUID;

@JBossLog
@GrpcService
@RegisterInterceptor(LogInterceptor.class)
public class MetadataServiceRPC extends MetadataServiceGrpc.MetadataServiceImplBase {

    @Inject
    MetadataService metadataService;

    @Override
    public void getFileBlockList(GetFileBlockListRequest request, StreamObserver<GetFileBlockListResponse> responseObserver) {
        if (request == null || request.getFileID().isBlank() || request.getWsID().isBlank()) {
            throw new InvalidParameterException("Invalid request");
        }

        Uni.createFrom().item(() -> metadataService.getFileBlockList(request.getFileID(), request.getWsID(), 1))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().with(
                        blockList -> {
                            responseObserver.onNext(GetFileBlockListResponse.newBuilder()
                                    .addAllBlock(blockList)
                                    .build());
                            responseObserver.onCompleted();
                        },
                        failure -> {
                            log.error("Failed to check blocks " + failure);
                            responseObserver.onError(failure);
                        }
                );
    }

    @Override
    public void verifyLink(VerifyLinkRequest request, StreamObserver<VerifyLinkResponse> responseObserver) {
        if (request == null || request.getLink().isBlank()) {
            throw new InvalidParameterException("Invalid request");
        }

        Uni.createFrom().item(() -> metadataService.verifyLink(request.getLink()))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().with(
                        validity -> {
                            responseObserver.onNext(VerifyLinkResponse.newBuilder()
                                    .setValid(validity.valid())
                                    .setExpired(validity.expired())
                                    .build());
                            responseObserver.onCompleted();
                        },
                        failure -> {
                            log.error("Failed to verify link " + failure);
                            responseObserver.onError(failure);
                        }
                );
    }

    @Override
    public void getUserWorkspace(GetUserWorkspaceRequest request, StreamObserver<GetUserWorkspaceResponse> responseObserver) {
        if (request == null || request.getUserID().isBlank() || request.getWorkspaceID().isBlank()) {
            throw new InvalidParameterException("Invalid request");
        }

        var userID = UUID.fromString(request.getUserID());
        Uni.createFrom().item(() -> metadataService.existsUserWorkspace(userID, request.getWorkspaceID()))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().with(
                        exists -> {
                            responseObserver.onNext(GetUserWorkspaceResponse.newBuilder()
                                    .setExists(exists)
                                    .build());
                            responseObserver.onCompleted();
                        },
                        failure -> {
                            log.error("Failed to check existing workspace user " + failure);
                            responseObserver.onError(failure);
                        }
                );
    }
}
