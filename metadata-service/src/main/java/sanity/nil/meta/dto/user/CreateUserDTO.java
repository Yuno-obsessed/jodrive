package sanity.nil.meta.dto.user;

import java.util.UUID;

public record CreateUserDTO(
        UUID id,
        String username,
        String email,
        String password
) { }
