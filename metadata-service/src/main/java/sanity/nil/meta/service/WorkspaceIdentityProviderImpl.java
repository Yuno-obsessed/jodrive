package sanity.nil.meta.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import sanity.nil.security.KeycloakIdentityProvider;
import sanity.nil.security.WorkspaceIdentityProvider;

import java.util.UUID;

@RequestScoped
public class WorkspaceIdentityProviderImpl extends KeycloakIdentityProvider implements WorkspaceIdentityProvider {

    @Inject
    MetadataService metadataService;

    public boolean identityBelongsToWorkspace(UUID userID, String workspaceID) {
        return metadataService.existsUserWorkspace(userID, workspaceID);
    }
}
