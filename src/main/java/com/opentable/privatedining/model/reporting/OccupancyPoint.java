package com.opentable.privatedining.model.reporting;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OccupancyPoint {

    private LocalDateTime slotStart;

    private LocalDateTime slotEnd;

    private Integer capacity;

    private Integer occupancy;

    private Double occupancyRate;
}
