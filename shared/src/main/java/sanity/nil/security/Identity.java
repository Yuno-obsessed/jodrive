package sanity.nil.security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter(value = AccessLevel.PACKAGE)
public class Identity {

    private UUID userID;
    private String username;
    private List<Role> roles;

    public boolean hasRole(final Role role) {
        return roles.contains(role);
    }
}
