package com.opentable.privatedining.exception;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class InvalidReservationException extends RuntimeException {

    public InvalidReservationException(LocalTime restaurantStart, LocalTime restaurantEnd,
        LocalDateTime reservationStart, LocalDateTime reservationEnd) {
        super("Reservation time " + reservationStart + " - " + reservationEnd +
            " is outside the restaurant operating hours of " + restaurantStart + " - " + restaurantEnd);
    }

    public InvalidReservationException(String message) {
        super(message);
    }
}