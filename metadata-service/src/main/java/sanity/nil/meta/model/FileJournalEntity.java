package sanity.nil.meta.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sanity.nil.meta.consts.FileState;
import sanity.nil.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileJournalEntity {
    private Long workspaceID;
    private String path;
    private Long size;
    private FileState state;
    private Collection<String> blocks;
    private UUID uploaderID;

    public FileJournalEntity create() {
        return null;
    }

    public String getBlockList() {
        if (CollectionUtils.isNotEmpty(blocks)) {
            return blocks.stream().collect(Collectors.joining(","));
        } else {
            return null;
        }
    }

    private List<String> getBlocksFromBlockList(String blocklist) {
        return Arrays.stream(blocklist.split(",")).collect(Collectors.toCollection(ArrayList::new));
    }
}
