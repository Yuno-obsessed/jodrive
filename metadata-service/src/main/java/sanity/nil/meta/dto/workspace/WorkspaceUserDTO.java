package sanity.nil.meta.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceUserDTO {
    public UUID id;
    public String username;
    public String avatarURL;
    public String role;
}
