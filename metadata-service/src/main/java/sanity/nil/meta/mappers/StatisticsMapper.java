package sanity.nil.meta.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import sanity.nil.meta.dto.user.StatisticsDTO;
import sanity.nil.meta.model.UserStatisticsModel;

@Mapper(componentModel = "cdi")
public interface StatisticsMapper {

    @Mappings({
            @Mapping(source = "id.statisticsID", target = "id"),
            @Mapping(source = "statistics.name", target = "name"),
            @Mapping(source = "statistics.description", target = "description"),
    })
    StatisticsDTO entityToDTO(UserStatisticsModel statisticsModel);
}
