package sanity.nil.meta.dto.block;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public record BlockMetadata(
        UUID correlationID,
        Collection<String> missingBlocks
) {
    public BlockMetadata(UUID correlationID) {
        this(correlationID, new HashSet<>());
    }
}