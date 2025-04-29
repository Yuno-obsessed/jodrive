package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

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

    private String filename;

    @Column(name = "content_type")
    private String contentType;

    private Long size;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private ZonedDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private ZonedDateTime updatedAt;

    public FileModel(String filename, String contentType, Long size) {
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }
}
