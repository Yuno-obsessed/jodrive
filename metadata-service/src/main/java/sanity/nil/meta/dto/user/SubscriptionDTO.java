package sanity.nil.meta.dto.user;

import java.math.BigDecimal;

public record SubscriptionDTO(
        Short id,
        String title,
        String description,
        BigDecimal storageLimit,
        Integer workspacesLimit
) { }
