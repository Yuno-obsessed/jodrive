package sanity.nil.meta.dto.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class UserBaseDTO {
    public UUID id;
    public String username;
    public String email;
    public SubscriptionDTO subscription;
    public String avatarURL;
    public LocalDateTime createdAt;
}
