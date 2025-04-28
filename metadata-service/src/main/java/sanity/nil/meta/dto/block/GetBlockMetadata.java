package sanity.nil.meta.dto.block;

import java.util.Set;
import java.util.UUID;

public record GetBlockMetadata(
        UUID correlationID,
        Long workspaceID,
        String filename,
        Set<BlockInfo> blocks,
        Integer lastBlockSize
) {
    public static class BlockInfo {
        public String hash;
        public Integer position;
    }
}
