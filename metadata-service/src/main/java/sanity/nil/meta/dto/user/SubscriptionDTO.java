package sanity.nil.meta.dto.user;

public record SubscriptionDTO(
        Short id,
        String title,
        String description,
        Long storageLimit,
        Integer workspacesLimit
) { }
