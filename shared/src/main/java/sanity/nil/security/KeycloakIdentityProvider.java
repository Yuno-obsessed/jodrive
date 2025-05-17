package sanity.nil.security;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import sanity.nil.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

@RequestScoped
@Named("keycloakIdentityProvider")
public class KeycloakIdentityProvider implements IdentityProvider {

    @Inject
    JsonWebToken jwt;
    @Inject
    SecurityConfiguration securityConfiguration;

    @Override
    public Identity getIdentity() {
        var identity = new Identity();
        JsonObject realmAccess = jwt.getClaim("realm_access");
        var roleList = realmAccess.getJsonArray("roles");

        identity.setUserID(UUID.fromString(jwt.getSubject()));
        identity.setUsername(jwt.getClaim("preferred_username").toString());
        identity.setEmail(jwt.getClaim("email").toString());
        identity.setRoles(IntStream.range(0, roleList.size())
                .mapToObj(i -> Role.fromName(roleList.getString(i)))
                .filter(Objects::nonNull).toList());
        return identity;
    }

    /**
    * @throws ForbiddenException if access token is invalid or not all claims are valid
    **/
    @Override
    public Identity getCheckedIdentity() {
        Identity identity;

        if (isEnabled()) {
            identity = getIdentity();
        } else {
            identity = getMockedIdentity();
        }

        if (!isValid(identity)) {
            throw new ForbiddenException();
        }

        return identity;
    }

    private boolean isValid(Identity identity) {
        return identity != null && identity.getUserID() != null
                && CollectionUtils.isNotEmpty(identity.getRoles());
    }

    private boolean isEnabled() {
        return securityConfiguration.enabled().isPresent() && securityConfiguration.enabled().get();
    }

    private Identity getMockedIdentity() {
        var identity = new Identity();
        identity.setUserID(securityConfiguration.defaultUserID().get());
        identity.setUsername("admin");
        identity.setEmail("example@gmail.com");
        identity.setRoles(List.of(Role.USER));
        return identity;
    }
}
