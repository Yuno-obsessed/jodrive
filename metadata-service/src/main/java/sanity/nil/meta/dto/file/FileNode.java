package sanity.nil.meta.dto.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Comparator;
import java.util.TreeSet;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "children")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString
public class FileNode implements Comparable<FileNode> {

    public String name;
    public FileInfo fileInfo;
    public TreeSet<FileNode> children = new TreeSet<>(Comparator.comparing(f -> f.name));

    public FileNode(String name, FileInfo fileInfo) {
        this.name = name;
        this.fileInfo = fileInfo;
    }

    @Override
    public int compareTo(FileNode other) {
        return this.name.compareTo(other.name);
    }
}
