package sanity.nil.meta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "links")
@Entity
public class LinkModel {

    @Id
    private String link;

    @Column(name = "times_used")
    private int timesUsed;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private ZonedDateTime createdAt;

    @Column(name = "expires_at", columnDefinition = "timestamptz")
    private ZonedDateTime expiresAt;
}
