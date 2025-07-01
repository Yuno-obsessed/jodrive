package sanity.nil.meta.mappers;

import org.mapstruct.Mapper;
import sanity.nil.meta.db.tables.records.UserSubscriptionsRecord;
import sanity.nil.meta.dto.user.SubscriptionDTO;

@Mapper(componentModel = "cdi")
public interface SubscriptionMapper {
    SubscriptionDTO entityToDTO(UserSubscriptionsRecord entity);
}
