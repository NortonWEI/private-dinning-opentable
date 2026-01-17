package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SpaceDTO {

    @Schema(description = "Unique identifier for the space", example = "123e4567-e89b-12d3-a456-426614174000", type = "string")
    private UUID id;

    @Schema(description = "Name of the space", example = "Private Dining Room A")
    private String name;

    @Schema(description = "Minimum capacity for the space", example = "2")
    private Integer minCapacity;

    @Schema(description = "Maximum capacity for the space", example = "12")
    private Integer maxCapacity;

    public SpaceDTO() {
    }

    public SpaceDTO(String name, Integer minCapacity, Integer maxCapacity) {
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }

    public SpaceDTO(UUID id, String name, Integer minCapacity, Integer maxCapacity) {
        this.id = id;
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }
}