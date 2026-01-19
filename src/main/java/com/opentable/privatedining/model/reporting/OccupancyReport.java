package com.opentable.privatedining.model.reporting;

import com.opentable.privatedining.common.Constant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OccupancyReport {

    private ObjectId restaurantId;

    private UUID spaceId;

    private LocalDateTime start;

    private LocalDateTime end;

    private final Long granularity = (long) Constant.BLOCK_INTERVAL;

    private OccupancyData restaurantData;

    private List<OccupancyData> spaceData;
}
