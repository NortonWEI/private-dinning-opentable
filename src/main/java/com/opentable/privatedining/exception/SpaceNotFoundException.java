package com.opentable.privatedining.exception;

import java.util.UUID;
import org.bson.types.ObjectId;

public class SpaceNotFoundException extends RuntimeException {

    public SpaceNotFoundException(ObjectId restaurantId, UUID spaceId) {
        super("Space not found with ID: " + spaceId.toString() + " in restaurant: " + restaurantId.toString());
    }

    public SpaceNotFoundException(String message) {
        super(message);
    }
}