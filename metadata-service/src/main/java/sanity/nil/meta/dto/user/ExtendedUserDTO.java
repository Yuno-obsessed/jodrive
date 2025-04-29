package sanity.nil.meta.dto.user;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ExtendedUserDTO extends UserBaseDTO {
    public List<StatisticsDTO> statistics;
}
