package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_journal")
@Entity
public class FileJournalModel {

    @EmbeddedId
    private FileJournalIDModel id;

    @ManyToOne
    @MapsId("workspaceID")
    @JoinColumn(name = "ws_id", columnDefinition = "bigint")
    private WorkspaceModel workspace;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @MapsId("fileID")
    @JoinColumn(name = "file_id", columnDefinition = "bigint ")
    private FileModel file;

//    @Column(columnDefinition = "tinyint(1)")
//    private Short latest;
    @Column(columnDefinition = "text")
    private String blocklist;
    @Column(name = "history_id")
    private Integer historyID;

    public FileJournalModel(WorkspaceModel workspace, FileModel file, String blocklist,
                            Integer historyID, Integer version) {
        this.id = new FileJournalIDModel(workspace.getId(), file.getId(), version);
        this.workspace = workspace;
        this.file = file;
        this.blocklist = blocklist;
        this.historyID = historyID;
    }

    //PK workspaceID, id
    @Embeddable
    public record FileJournalIDModel (
            @Column(name = "ws_id")
            Long workspaceID,
            @Column(name = "file_id")
            Long fileID,
            Integer version
    ) { }
}
