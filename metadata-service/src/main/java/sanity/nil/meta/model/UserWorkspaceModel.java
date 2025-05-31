package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import sanity.nil.meta.consts.WsRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_workspaces")
@Entity
public class UserWorkspaceModel {

    @EmbeddedId
    private UserWorkspaceIDModel id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("workspaceID")
    @JoinColumn(name = "ws_id", columnDefinition = "bigint")
    private WorkspaceModel workspace;

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(name = "user_id", columnDefinition = "bigint")
    private UserModel user;

    @Enumerated(EnumType.STRING)
    private WsRole role;

    @CreationTimestamp
    @Column(name = "joined_at", columnDefinition = "timestamptz")
    private LocalDateTime joinedAt;

    @Embeddable
    public record UserWorkspaceIDModel(
            @Column(name = "ws_id")
            Long workspaceID,
            @Column(name = "user_id")
            UUID userID
    ) {}

    public UserWorkspaceModel(WorkspaceModel workspace, UserModel user, WsRole role) {
        this.id = new UserWorkspaceIDModel(workspace.getId(), user.getId());
        this.workspace = workspace;
        this.user = user;
        this.role = role;
    }
}
