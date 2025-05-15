package sanity.nil.meta.cache.model;

import sanity.nil.meta.consts.FileState;

import java.util.List;

public record FileMetadata(
        String path,
        List<String> blocks,
        Long size,
        FileState state
) { }