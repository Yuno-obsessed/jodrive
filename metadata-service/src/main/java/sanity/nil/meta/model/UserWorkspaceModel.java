package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String role;

    @Embeddable
    public record UserWorkspaceIDModel(
            @Column(name = "ws_id")
            Long workspaceID,
            @Column(name = "user_id")
            UUID userID
    ) {}

    public UserWorkspaceModel(WorkspaceModel workspace, UserModel user, String role) {
        this.id = new UserWorkspaceIDModel(workspace.getId(), user.getId());
        this.workspace = workspace;
        this.user = user;
        this.role = role;
    }
}
