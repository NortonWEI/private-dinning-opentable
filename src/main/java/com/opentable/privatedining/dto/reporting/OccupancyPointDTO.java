package com.opentable.privatedining.dto.reporting;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.opentable.privatedining.jsonserializer.TwoDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OccupancyPointDTO {

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @Schema(type = "string", description = "Start time of the current slot", example = "15-01-2026 19:30", pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime slotStart;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @Schema(type = "string", description = "End time of the current slot", example = "15-01-2026 20:00", pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime slotEnd;

    @Schema(description = "Capacity of the restaurant/space during the current slot", example = "50", type = "integer")
    private Integer capacity;

    @Schema(description = "Occupancy of the restaurant/space during the current slot", example = "30", type = "integer")
    private Integer occupancy;

    @Schema(description = "Occupancy rate (occupancy/capacity) during the current slot", example = "0.6", type = "number", format = "double")
    @JsonSerialize(using = TwoDecimalSerializer.class)
    private Double occupancyRate;
}
