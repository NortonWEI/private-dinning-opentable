package com.opentable.privatedining.mapper.reporting;

import com.opentable.privatedining.dto.reporting.OccupancyReportDTO;
import com.opentable.privatedining.model.reporting.OccupancyReport;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OccupancyDataMapper.class, OccupancyPointMapper.class})
public interface OccupancyReportMapper {

    OccupancyReport toModel(OccupancyReportDTO occupancyReportDTO);

    OccupancyReportDTO toDto(OccupancyReport occupancyReport);

    OccupancyReport copy(OccupancyReport source);

    default String objectIdToString(ObjectId id) {
        return id == null ? null : id.toHexString();
    }

    default ObjectId stringToObjectId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return new ObjectId(id);
    }

    default String uuidToString(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    default UUID stringToUuid(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return UUID.fromString(id);
    }
}
