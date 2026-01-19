package com.opentable.privatedining.mapper.reporting;

import com.opentable.privatedining.dto.reporting.OccupancyPointDTO;
import com.opentable.privatedining.model.reporting.OccupancyPoint;
import com.opentable.privatedining.model.reporting.OccupancyReport;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OccupancyPointMapper {

    OccupancyPointDTO toDto(OccupancyPoint occupancyPoint);

    OccupancyReport toModel(OccupancyPointDTO occupancyPointDTO);
}
