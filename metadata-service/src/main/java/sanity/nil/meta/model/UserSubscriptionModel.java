package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_subscriptions")
@Entity
public class UserSubscriptionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;
    private String title;
    private String description;
    @Column(name = "storage_limit")
    private Long storageLimit;
    @Column(name = "workspaces_limit")
    private Integer workspacesLimit;
}
