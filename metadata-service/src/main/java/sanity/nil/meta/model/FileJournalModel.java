package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sanity.nil.meta.consts.FileState;

import java.io.Serializable;
import java.time.LocalDateTime;

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

    @Column(name = "file_id", insertable = false, updatable = false)
    private Long fileID;

    @Column(columnDefinition = "smallint")
    private Short latest;

    private String filename;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserModel uploader;

    @Enumerated(EnumType.STRING)
    private FileState state;

    private Long size;

    @Column(columnDefinition = "text")
    private String blocklist;
    @Column(name = "history_id")
    private Integer historyID;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private LocalDateTime updatedAt;

    public FileJournalModel(WorkspaceModel workspace, String filename, UserModel uploader, FileState state,
                            Long size, String blocklist, Integer historyID) {
        this.id = new FileJournalIDModel(workspace.getId(), null);
        this.workspace = workspace;
        this.filename = filename;
        this.uploader = uploader;
        this.state = state;
        this.size = size;
        this.blocklist = blocklist;
        this.historyID = historyID;
    }

    public void setNewID(Long workspaceID, Long fileID) {
        this.id = new FileJournalIDModel(workspaceID, fileID);
        this.fileID = fileID;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class FileJournalIDModel implements Serializable {
        @Column(name = "ws_id")
        private Long workspaceID;
        @Column(name = "file_id")
        private Long fileID;
    }
}
