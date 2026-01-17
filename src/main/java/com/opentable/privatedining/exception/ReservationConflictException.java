package com.opentable.privatedining.exception;

import java.time.LocalDateTime;
import java.util.UUID;
import org.bson.types.ObjectId;

public class ReservationConflictException extends RuntimeException {

    public ReservationConflictException(ObjectId restaurantId, UUID spaceId, LocalDateTime startTime,
        LocalDateTime endTime) {
        super("Reservation conflict: the requested time slot (" + startTime + " to " + endTime +
            ") overlaps with an existing reservation for space " + spaceId + " in restaurant " + restaurantId);
    }

    public ReservationConflictException(String message) {
        super(message);
    }
}