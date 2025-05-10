package sanity.nil.meta.dto.file;

import java.util.UUID;

public record FileFilters(
        Long wsID,
        UUID userID,
        String name,
        Boolean deleted,
        Integer page,
        Integer size
) { }
