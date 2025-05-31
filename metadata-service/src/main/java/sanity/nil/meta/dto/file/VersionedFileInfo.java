package sanity.nil.meta.dto.file;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class VersionedFileInfo extends FileInfo {
    public Integer versions;
}
