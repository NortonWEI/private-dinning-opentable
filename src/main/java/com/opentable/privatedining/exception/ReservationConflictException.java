package com.opentable.privatedining.exception;

import java.time.LocalDateTime;
import java.util.UUID;
import org.bson.types.ObjectId;

public class ReservationConflictException extends RuntimeException {

    public ReservationConflictException(ObjectId restaurantId, UUID spaceId, LocalDateTime startTime,
        LocalDateTime endTime, int minCapacity, int maxCapacity, int proposedCapacity,
        LocalDateTime conflictStartTime) {
        super(String.format(
            "Reservation conflict: the requested time slot (%s to %s) breaks the capacity constraints (%d - %d) for space %s in restaurant %s. Invalid capacity %d starts from %s",
            startTime, endTime, minCapacity, maxCapacity, spaceId, restaurantId, proposedCapacity, conflictStartTime));
    }

    public ReservationConflictException(String message) {
        super(message);
    }
}