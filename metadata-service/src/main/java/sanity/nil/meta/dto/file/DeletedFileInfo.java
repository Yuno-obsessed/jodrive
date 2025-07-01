package sanity.nil.meta.dto.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
public class DeletedFileInfo extends FileInfo {
    public String path;
    public int versions;
    public WorkspaceUserDTO deletedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    public OffsetDateTime deletedAt;
}
