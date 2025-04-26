package sanity.nil.block.presentation.grpc;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.block.service.BlockService;
import sanity.nil.grpc.LogInterceptor;
import sanity.nil.grpc.block.*;

@GrpcService
@JBossLog
@RegisterInterceptor(LogInterceptor.class)
public class BlockServiceRPC extends BlockServiceGrpc.BlockServiceImplBase {

    @Inject
    BlockService blockService;

    @Override
    @Blocking
    public void checkBlocksExistence(CheckBlocksExistenceRequest request, StreamObserver<CheckBlocksExistenceResponse> responseObserver) {
        Uni.createFrom().item(() -> blockService.checkBlocksExistence(request.getHashList()))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().with(
                        missingBlocks -> {
                            responseObserver.onNext(CheckBlocksExistenceResponse.newBuilder()
                                    .addAllMissingBlocks(missingBlocks)
                                    .build());
                            responseObserver.onCompleted();
                        },
                        failure -> {
                            log.error("Failed to check blocks", failure);
                            responseObserver.onError(failure);
                        }
                );
    }

    @Override
    @Blocking
    public void deleteBlocks(DeleteBlocksRequest request, StreamObserver<DeleteBlocksResponse> responseObserver) {
        Uni.createFrom().item(() -> blockService.deleteBlocks(request.getHashList()))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().with(
                        response -> {
                            responseObserver.onNext(DeleteBlocksResponse.newBuilder()
                                    .setCode(response ? Code.success : Code.failure)
                                    .build());
                            responseObserver.onCompleted();
                        },
                        failure -> {
                            log.error("Failed to delete blocks", failure);
                            responseObserver.onError(failure);
                        }
                );
    }
}
