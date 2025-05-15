package sanity.nil.meta.dto.file;

import io.smallrye.common.constraint.NotNull;

public record CreateDirectory(
        @NotNull
        Long workspaceID,
        @NotNull
        String path,
        @NotNull
        String name
) { }
