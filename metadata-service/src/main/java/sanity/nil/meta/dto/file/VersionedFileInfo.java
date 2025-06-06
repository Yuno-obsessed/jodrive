package sanity.nil.meta.dto.file;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class VersionedFileInfo extends FileInfo {
    public List<FileVersion> versions;
}
