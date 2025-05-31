package sanity.nil.meta.dto.workspace;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sanity.nil.meta.consts.WsRole;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceUserDTO {
    public UUID id;
    public String username;
    public String avatarURL;
    public WsRole role;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    public LocalDateTime joinedAt;
}
