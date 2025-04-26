package sanity.nil.meta.presentation.grpc;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.grpc.LogInterceptor;
import sanity.nil.grpc.meta.GetFileBlockListRequest;
import sanity.nil.grpc.meta.GetFileBlockListResponse;
import sanity.nil.grpc.meta.MetadataServiceGrpc;
import sanity.nil.meta.service.MetadataService;

import java.security.InvalidParameterException;

@JBossLog
@GrpcService
@RegisterInterceptor(LogInterceptor.class)
public class MetadataServiceRPC extends MetadataServiceGrpc.MetadataServiceImplBase {

    @Inject
    MetadataService metadataService;

    @Override
    public void getFileBlockList(GetFileBlockListRequest request, StreamObserver<GetFileBlockListResponse> responseObserver) {
        if (request == null || request.getId().isBlank()) {
            throw new InvalidParameterException("Invalid request");
        }

        Uni.createFrom().item(() -> metadataService.getFileBlockList(request.getId()))
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

}
