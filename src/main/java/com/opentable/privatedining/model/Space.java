package com.opentable.privatedining.model;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Space {

    private UUID id;
    private String name;
    private Integer minCapacity;
    private Integer maxCapacity;

    public Space() {
        this.id = UUID.randomUUID();
    }

    public Space(String name, Integer minCapacity, Integer maxCapacity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }
}


