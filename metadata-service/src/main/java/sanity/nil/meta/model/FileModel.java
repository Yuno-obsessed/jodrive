package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @ManyToOne(fetch = FetchType.LAZY)
    private UserModel uploader;

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

    public FileModel(UserModel uploader, String filename, String contentType, Long size) {
        this.uploader = uploader;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }
}
