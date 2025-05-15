package sanity.nil.meta.dto.block;

import sanity.nil.meta.dto.file.FileInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public record BlockMetadata(
        UUID correlationID,
        Collection<String> missingBlocks,
        FileInfo fileInfo
) {
    public BlockMetadata(UUID correlationID, FileInfo fileInfo) {
        this(correlationID, new HashSet<>(), fileInfo);
    }
}