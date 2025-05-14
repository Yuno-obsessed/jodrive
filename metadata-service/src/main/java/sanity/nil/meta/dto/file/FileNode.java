package sanity.nil.meta.dto.file;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class FileNode {

    public String name;
    public FileInfo fileInfo;
    public List<FileNode> children = new ArrayList<>();
//    public boolean isDirectory;
//    public int fileCount = 0;
}
