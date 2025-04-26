package sanity.nil.meta.mappers;

import org.mapstruct.Mapper;
import sanity.nil.meta.dto.user.SubscriptionDTO;
import sanity.nil.meta.model.UserSubscriptionModel;

@Mapper(componentModel = "cdi")
public interface SubscriptionMapper {
    SubscriptionDTO entityToDTO(UserSubscriptionModel entity);
}
