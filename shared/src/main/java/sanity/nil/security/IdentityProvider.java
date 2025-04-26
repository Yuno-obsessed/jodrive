package sanity.nil.security;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import sanity.nil.util.CollectionUtils;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

@ApplicationScoped
public class IdentityProvider {

    @Inject
    JsonWebToken jwt;

    public Identity getIdentity() {
        var identity = new Identity();
        JsonObject realmAccess = jwt.getClaim("realm_access");
        var roleList = realmAccess.getJsonArray("roles");

        identity.setUserID(UUID.fromString(jwt.getSubject()));
        identity.setUsername(jwt.getClaim("preferred_username").toString());
        identity.setRoles(IntStream.range(0, roleList.size())
                .mapToObj(i -> Role.fromName(roleList.getString(i)))
                .filter(Objects::nonNull).toList());
        return identity;
    }

    /**
    * @throws ForbiddenException if access token is invalid or not all claims are valid
    **/
    public Identity getCheckedIdentity() {
        var identity = getIdentity();
        if (identity == null || identity.getUserID() == null
                || CollectionUtils.isEmpty(identity.getRoles())
        ) {
            throw new ForbiddenException();
        }
        return identity;
    }
}
