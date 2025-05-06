package sanity.nil.security;

import io.smallrye.mutiny.Uni;
import sanity.nil.exceptions.WorkspaceIdentityException;

import java.time.Duration;
import java.util.UUID;

public interface WorkspaceIdentityProvider extends IdentityProvider {

    default Identity getIdentity(String workspaceID) {
        return getIdentityUni(workspaceID)
                .await().atMost(Duration.ofSeconds(2));
    }

    default Uni<Identity> getIdentityUni(String workspaceID) {
        var identity = getCheckedIdentity();
        return identityBelongsToWorkspace(identity.getUserID(), workspaceID)
                .flatMap(belongs -> {
                    if (belongs) {
                        return Uni.createFrom().item(identity);
                    } else {
                        return Uni.createFrom().failure(new WorkspaceIdentityException(workspaceID));
                    }
                });
    }

    Uni<Boolean> identityBelongsToWorkspace(UUID userID, String workspaceID);

}
