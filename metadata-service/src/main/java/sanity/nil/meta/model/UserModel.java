package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
@Entity
public class UserModel {

    @Id
    private UUID id;
    private String username;
    private String email;
    private String password;
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_id")
    private UserSubscriptionModel subscription;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private LocalDateTime updatedAt;

    public UserModel(String username, String email, String password, UserSubscriptionModel subscription) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
        this.subscription = subscription;
    }
}
