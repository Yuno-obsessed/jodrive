package sanity.nil.meta.mappers;

import org.mapstruct.Mapper;
import sanity.nil.meta.db.tables.records.UsersRecord;
import sanity.nil.meta.dto.user.ExtendedUserDTO;
import sanity.nil.meta.dto.user.UserBaseDTO;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    UserBaseDTO entityToDTO(UsersRecord user);
    ExtendedUserDTO entityToExtendedDTO(UsersRecord user);
}
