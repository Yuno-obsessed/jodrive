package sanity.nil.meta.dto.block;

import java.util.Set;
import java.util.UUID;

public record GetBlocksMetadata(
        UUID correlationID,
        Long workspaceID,
        String filename,
        Set<BlockInfo> blocks,
        Integer lastBlockSize
) {
    public record BlockInfo(
            String hash,
            Integer position
    ) { }
}
