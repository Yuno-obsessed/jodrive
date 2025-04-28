package sanity.nil.meta.dto.user;

import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
public class ExtendedUserDTO extends UserBaseDTO {
    public List<StatisticsDTO> statistics;
}
