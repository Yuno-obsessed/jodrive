package sanity.nil.block.service;

import io.quarkus.grpc.GrpcClient;
import jakarta.enterprise.context.RequestScoped;
import sanity.nil.grpc.meta.GetUserWorkspaceRequest;
import sanity.nil.grpc.meta.MutinyMetadataServiceGrpc;
import sanity.nil.security.KeycloakIdentityProvider;
import sanity.nil.security.WorkspaceIdentityProvider;

import java.time.Duration;
import java.util.UUID;

@RequestScoped
public class WorkspaceIdentityProviderImpl extends KeycloakIdentityProvider implements WorkspaceIdentityProvider {

    @GrpcClient("metadataService")
    MutinyMetadataServiceGrpc.MutinyMetadataServiceStub metadataClient;

    @Override
    public boolean identityBelongsToWorkspace(UUID userID, String workspaceID) {
        var response = metadataClient.getUserWorkspace(
                GetUserWorkspaceRequest.newBuilder().setWorkspaceID(workspaceID)
                        .setUserID(userID.toString()).build()
        );
        return response.await().atMost(Duration.ofSeconds(2)).getExists();
    }
}
