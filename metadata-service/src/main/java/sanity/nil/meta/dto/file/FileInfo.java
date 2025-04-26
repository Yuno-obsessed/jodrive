package sanity.nil.meta.dto.file;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    public String filename;
    public String contentType;
    public ZonedDateTime createdAt;
}
