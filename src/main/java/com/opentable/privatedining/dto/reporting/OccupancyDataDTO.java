package com.opentable.privatedining.dto.reporting;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OccupancyDataDTO {

    @Schema(description = "ID of the restaurant/space", example = "507f1f77bcf86cd799439011", type = "string")
    private String id;

    @Schema(description = "Name of the restaurant/space", example = "Private Dining Room A")
    private String name;

    @Schema(description = "Occupancy data points of the restaurant/space")
    private List<OccupancyPointDTO> points;
}
