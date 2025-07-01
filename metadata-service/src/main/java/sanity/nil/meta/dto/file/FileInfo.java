package sanity.nil.meta.dto.file;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    public Long id;
    public Long workspaceID;
    public String name;
    public boolean isDirectory;
    public long size;
    public UUID uploader;
    public String uploaderName;
    public String uploaderAvatar;
    public OffsetDateTime uploadedAt;
}
