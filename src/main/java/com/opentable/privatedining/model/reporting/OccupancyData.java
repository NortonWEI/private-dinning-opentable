package com.opentable.privatedining.model.reporting;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OccupancyData {

    private String id;

    private String name;

    private List<OccupancyPoint> points;
}
