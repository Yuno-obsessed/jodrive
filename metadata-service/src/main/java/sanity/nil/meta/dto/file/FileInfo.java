package sanity.nil.meta.dto.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    public LocalDateTime uploadedAt;
}
