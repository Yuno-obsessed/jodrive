package sanity.nil.block.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import sanity.nil.block.consts.BlockStatus;

import java.time.ZonedDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blocks")
@Entity
public class BlockModel {

    @Id
    private String hash;

    @Enumerated(EnumType.STRING)
    private BlockStatus status;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private ZonedDateTime createdAt;

    public BlockModel(String hash) {
        this.hash = hash;
    }
}
