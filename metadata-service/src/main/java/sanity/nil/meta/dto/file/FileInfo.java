package sanity.nil.meta.dto.file;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    public String filename;
    public String contentType;
    public LocalDateTime createdAt;
}
