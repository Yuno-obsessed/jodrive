package sanity.nil.block.service;

import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.grpc.meta.GetUserWorkspaceRequest;
import sanity.nil.grpc.meta.MutinyMetadataServiceGrpc;
import sanity.nil.security.KeycloakIdentityProvider;
import sanity.nil.security.WorkspaceIdentityProvider;

import java.util.UUID;

@RequestScoped
@JBossLog
public class WorkspaceIdentityProviderImpl extends KeycloakIdentityProvider implements WorkspaceIdentityProvider {

    @GrpcClient("metadataService")
    MutinyMetadataServiceGrpc.MutinyMetadataServiceStub metadataClient;

    @Override
    public Uni<Boolean> identityBelongsToWorkspace(UUID userID, String workspaceID) {
        var response = metadataClient.getUserWorkspace(
                GetUserWorkspaceRequest.newBuilder().setWorkspaceID(workspaceID)
                        .setUserID(userID.toString()).build()
        );
        return response.flatMap(resp -> Uni.createFrom().item(resp.getExists()));
    }
}
