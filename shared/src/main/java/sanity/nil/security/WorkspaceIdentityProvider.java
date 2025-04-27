package sanity.nil.security;

import sanity.nil.exceptions.WorkspaceIdentityException;

import java.util.UUID;

public interface WorkspaceIdentityProvider extends IdentityProvider {

    default Identity getIdentity(String workspaceID) {
        var identity = getCheckedIdentity();
        if (identityBelongsToWorkspace(identity.getUserID(), workspaceID)) {
            return identity;
        } else {
            throw new WorkspaceIdentityException(workspaceID);
        }
    }

    boolean identityBelongsToWorkspace(UUID userID, String workspaceID);
}
