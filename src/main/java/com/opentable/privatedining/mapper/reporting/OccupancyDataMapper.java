package com.opentable.privatedining.mapper.reporting;

import com.opentable.privatedining.dto.reporting.OccupancyDataDTO;
import com.opentable.privatedining.model.reporting.OccupancyData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OccupancyPointMapper.class})
public interface OccupancyDataMapper {

    OccupancyData toModel(OccupancyDataDTO occupancyDataDTO);

    OccupancyDataDTO toDto(OccupancyData occupancyData);
}
