package com.opentable.privatedining.dto.reporting;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OccupancyReportDTO {

    @Schema(description = "ID of the restaurant", example = "507f1f77bcf86cd799439011", type = "string")
    private String restaurantId;

    @Schema(description = "ID of the space within the restaurant", example = "123e4567-e89b-12d3-a456-426614174000", type = "string")
    private String spaceId;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @Schema(type = "string", description = "Start time of the occupancy report", example = "15-01-2026 19:30", pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime start;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @Schema(type = "string", description = "End time of the occupancy report", example = "16-01-2026 19:30", pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime end;

    @Schema(description = "Occupancy data for the restaurant")
    private OccupancyDataDTO restaurantData;

    @Schema(description = "Occupancy data for the specific space")
    private List<OccupancyDataDTO> spaceData;

    public OccupancyReportDTO(String restaurantId, String spaceId, LocalDateTime start, LocalDateTime end) {
        this.restaurantId = restaurantId;
        this.spaceId = spaceId;
        this.start = start;
        this.end = end;
    }
}
