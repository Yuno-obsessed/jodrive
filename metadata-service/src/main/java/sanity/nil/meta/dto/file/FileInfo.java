package sanity.nil.meta.dto.file;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    public String filename;
    public long size;
    public UUID uploader;
    public LocalDateTime createdAt;
}
