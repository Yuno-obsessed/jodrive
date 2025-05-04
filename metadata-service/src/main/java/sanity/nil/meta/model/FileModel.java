package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sanity.nil.meta.consts.FileState;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "files")
@Entity
public class FileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private Long id;

    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserModel uploader;

    @Enumerated(EnumType.STRING)
    private FileState state;

    private String filename;

    @Column(name = "content_type")
    private String contentType;

    private Long size;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private LocalDateTime updatedAt;

    public FileModel(Integer version, UserModel uploader, FileState state, String filename, String contentType, Long size) {
        this.version = version;
        this.state = state;
        this.uploader = uploader;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }
}
