package sanity.nil.meta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "links")
@Entity
public class LinkModel {

    @Id
    private String link;

    // Don't need issuer info so just persist his id
    private UUID issuer;

    @Column(name = "times_used")
    private int timesUsed;

    @Column(name = "created_at", columnDefinition = "timestamptz")
    private LocalDateTime createdAt;

    @Column(name = "expires_at", columnDefinition = "timestamptz")
    private LocalDateTime expiresAt;

    public LinkModel(UUID issuer, String link, LocalDateTime expiresAt) {
        this.issuer = issuer;
        this.link = link;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }
}
