package sanity.nil.meta.dto.user;

public record CreateUserDTO(
        String username,
        String email,
        String password
) { }
