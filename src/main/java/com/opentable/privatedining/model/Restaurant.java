package com.opentable.privatedining.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "restaurants")
@Getter
@Setter
public class Restaurant {

    @Id
    private ObjectId id;
    private String name;
    private String address;
    private String cuisineType;
    private Integer capacity;
    private LocalTime startTime;
    private LocalTime endTime;

    private List<Space> spaces;

    public Restaurant() {
        this.spaces = new ArrayList<>();
    }

    public Restaurant(String name, String address, String cuisineType, Integer capacity) {
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.capacity = capacity;
        this.spaces = new ArrayList<>();
    }

    public Restaurant(String name, String address, String cuisineType, Integer capacity, LocalTime startTime,
        LocalTime endTime) {
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.spaces = new ArrayList<>();
    }
}