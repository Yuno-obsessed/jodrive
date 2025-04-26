package sanity.nil.meta.dto.file;

import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;

public class FileInfoExtended extends FileInfo {
    public WorkspaceUserDTO lastChangedBy;
    public int versions;
    public long size;
}
