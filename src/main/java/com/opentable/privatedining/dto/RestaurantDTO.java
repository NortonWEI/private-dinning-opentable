package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RestaurantDTO {

    @Schema(description = "Unique identifier for the restaurant", example = "507f1f77bcf86cd799439011", type = "string")
    private String id;

    @Schema(description = "Name of the restaurant", example = "The French Laundry")
    private String name;

    @Schema(description = "Address of the restaurant", example = "6640 Washington St, Yountville, CA 94599")
    private String address;

    @Schema(description = "Type of cuisine served", example = "French")
    private String cuisineType;

    @Schema(description = "Total capacity of the restaurant", example = "100")
    private Integer capacity;

    @Schema(description = "Opening time of the restaurant", example = "10:00")
    private LocalTime startTime;

    @Schema(description = "Closing time of the restaurant", example = "22:00")
    private LocalTime endTime;

    @Schema(description = "List of spaces available in the restaurant")
    private List<SpaceDTO> spaces;

    public RestaurantDTO() {
        this.spaces = new ArrayList<>();
    }

    public RestaurantDTO(String name, String address, String cuisineType, Integer capacity) {
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.capacity = capacity;
        this.spaces = new ArrayList<>();
    }

    public RestaurantDTO(String id, String name, String address, String cuisineType, Integer capacity,
        List<SpaceDTO> spaces) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.capacity = capacity;
        this.spaces = spaces != null ? spaces : new ArrayList<>();
    }

    public RestaurantDTO(String id, String name, String address, String cuisineType, Integer capacity,
        LocalTime startTime, LocalTime endTime, List<SpaceDTO> spaces) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.spaces = spaces != null ? spaces : new ArrayList<>();
    }
}