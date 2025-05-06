package sanity.nil.meta.security;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import sanity.nil.meta.service.MetadataService;
import sanity.nil.security.KeycloakIdentityProvider;
import sanity.nil.security.WorkspaceIdentityProvider;

import java.util.UUID;

@RequestScoped
public class WorkspaceIdentityProviderImpl extends KeycloakIdentityProvider implements WorkspaceIdentityProvider {

    @Inject
    MetadataService metadataService;

    public Uni<Boolean> identityBelongsToWorkspace(UUID userID, String workspaceID) {
        return Uni.createFrom().item(metadataService.existsUserWorkspace(userID, workspaceID));
    }
}
