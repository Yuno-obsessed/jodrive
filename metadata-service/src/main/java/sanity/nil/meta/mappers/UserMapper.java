package sanity.nil.meta.mappers;

import org.mapstruct.Mapper;
import sanity.nil.meta.dto.user.ExtendedUserDTO;
import sanity.nil.meta.dto.user.UserBaseDTO;
import sanity.nil.meta.model.UserModel;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    UserBaseDTO entityToDTO(UserModel user);
    ExtendedUserDTO entityToExtendedDTO(UserModel user);
}
