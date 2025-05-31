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

    @Column(columnDefinition = "text")
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserModel uploader;

    @Enumerated(EnumType.STRING)
    private FileState state;

    private Long size;

    @Column(columnDefinition = "text")
    private String blocklist;
    @Column(name = "history_id")
    private Integer historyID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserModel updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private LocalDateTime updatedAt;

    public FileJournalModel(WorkspaceModel workspace, String path, UserModel uploader, FileState state, short latest,
                            Long size, String blocklist) {
        this.id = new FileJournalIDModel(workspace.getId(), null);
        this.workspace = workspace;
        this.path = path;
        this.uploader = uploader;
        this.state = state;
        this.latest = latest;
        this.size = size;
        this.blocklist = blocklist;
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
