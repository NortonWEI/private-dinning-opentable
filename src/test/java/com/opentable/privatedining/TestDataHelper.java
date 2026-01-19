package com.opentable.privatedining;

import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import org.bson.types.ObjectId;

public final class TestDataHelper {
    private TestDataHelper() {
        // Private constructor to prevent instantiation
    }

    public static Reservation createTestReservation(String customerEmail, int partySize) {
        Reservation reservation = new Reservation();
        reservation.setCustomerEmail(customerEmail);
        reservation.setPartySize(partySize);
        reservation.setRestaurantId(new ObjectId());
        reservation.setSpaceId(UUID.randomUUID());
        reservation.setStartTime(LocalDateTime.of(2026, 1, 30, 19, 0));
        reservation.setEndTime(LocalDateTime.of(2026, 1, 30, 22, 0));
        reservation.setStatus("CONFIRMED");
        return reservation;
    }

    public static Restaurant createTestRestaurant() {
        return new Restaurant(
            "Test Restaurant", "Address", "Cuisine", 50, LocalTime.of(11, 0), LocalTime.of(23, 0));
    }
}
