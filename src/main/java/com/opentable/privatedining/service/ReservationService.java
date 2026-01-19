package com.opentable.privatedining.service;

import com.opentable.privatedining.common.Constant;
import com.opentable.privatedining.exception.InvalidReservationException;
import com.opentable.privatedining.exception.ReservationConflictException;
import com.opentable.privatedining.exception.RestaurantNotFoundException;
import com.opentable.privatedining.exception.SpaceNotFoundException;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.ReservationRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final RestaurantService restaurantService;

    public ReservationService(ReservationRepository reservationRepository, RestaurantService restaurantService) {
        this.reservationRepository = reservationRepository;
        this.restaurantService = restaurantService;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(ObjectId id) {
        return reservationRepository.findById(id);
    }

    public Reservation createReservation(Reservation reservation) {
        // Basic validations
        if (!reservation.getStartTime().isAfter(LocalDateTime.now())) {
            throw new InvalidReservationException("Reservation must start in the future.");
        }

        // reservation duration must be positive and within 24 hours
        Duration duration = Duration.between(reservation.getStartTime(), reservation.getEndTime());
        if (duration.isNegative() || duration.isZero() || duration.toHours() > 24) {
            throw new InvalidReservationException("Reservation duration must be positive and within 24 hours.");
        }

        // Check if the reservation time is in a blocked period (currently only full and half-hour blocks allowed)
        if (reservation.getStartTime().getMinute() % Constant.BLOCK_INTERVAL_MIN
            != 0 || reservation.getEndTime().getMinute() % Constant.BLOCK_INTERVAL_MIN != 0) {
            throw new InvalidReservationException("Reservation times must be on the hour or half-hour.");
        }

        // Validate that the restaurant exists
        Optional<com.opentable.privatedining.model.Restaurant> restaurantOpt =
            restaurantService.getRestaurantById(reservation.getRestaurantId());
        if (restaurantOpt.isEmpty()) {
            throw new RestaurantNotFoundException(reservation.getRestaurantId());
        }

        Restaurant restaurant = restaurantOpt.get();
        Space space = restaurant.getSpaces().stream().filter(s -> s.getId().equals(reservation.getSpaceId()))
            .findFirst()
            .orElseThrow(() -> new SpaceNotFoundException(reservation.getRestaurantId(), reservation.getSpaceId()));

        // Check if the reservation is within restaurant operating hours
        if (!isWithinOperatingHours(reservation.getStartTime(), reservation.getEndTime(), restaurant.getStartTime(),
            restaurant.getEndTime())) {
            throw new InvalidReservationException(restaurant.getStartTime(), restaurant.getEndTime(),
                reservation.getStartTime(), reservation.getEndTime());
        }

        // Check for concurrent reservation conflicts
        if (!isValidConcurrentReservation(reservation.getRestaurantId(), reservation.getSpaceId(),
            space.getMinCapacity(), space.getMaxCapacity(), reservation.getStartTime(), reservation.getEndTime(),
            reservation.getPartySize())) {
            throw new ReservationConflictException(
                reservation.getRestaurantId(), reservation.getSpaceId(),
                reservation.getStartTime(), reservation.getEndTime(), space.getMinCapacity(), space.getMaxCapacity(),
                reservation.getPartySize(), reservation.getStartTime());
        }

        return reservationRepository.save(reservation);
    }

    private boolean isValidConcurrentReservation(ObjectId restaurantId, UUID spaceId, int spaceMinCapacity,
        int spaceMaxCapacity, LocalDateTime startTime, LocalDateTime endTime, int partySize) {
        List<Reservation> reservations = reservationRepository.findByRestaurantIdAndSpaceIdAndOverlap(
            restaurantId, spaceId, startTime, endTime);

        if (reservations.isEmpty()) {
            // no overlapping reservations found
            if (partySize > spaceMaxCapacity || partySize < spaceMinCapacity) {
                throw new ReservationConflictException(restaurantId, spaceId, startTime, endTime, spaceMinCapacity,
                    spaceMaxCapacity, partySize, startTime);
            }
        }

        // currently the time slots are blocked in half-hour increments
        long minuteInterval = Duration.between(startTime, endTime).toMinutes();
        if (minuteInterval % Constant.BLOCK_INTERVAL_MIN != 0) {
            // this should never happen due to earlier validation and our assumption, but just in case
            throw new InvalidReservationException("Reservation times must be in half-hour increments.");
        }
        for (int i = 0; i < (int) (minuteInterval / Constant.BLOCK_INTERVAL_MIN); i++) {
            LocalDateTime slotStart = startTime.plusMinutes((long) i * Constant.BLOCK_INTERVAL_MIN);
            LocalDateTime slotEnd = slotStart.plusMinutes(Constant.BLOCK_INTERVAL_MIN);
            List<Reservation> slotRes = reservations.stream()
                .filter(res -> res.getStartTime().isBefore(slotEnd) && res.getEndTime().isAfter(slotStart)).toList();
            int proposedPartySize = slotRes.stream().mapToInt(Reservation::getPartySize).sum() + partySize;
            if (proposedPartySize > spaceMaxCapacity || proposedPartySize < spaceMinCapacity) {
                throw new ReservationConflictException(restaurantId, spaceId, startTime, endTime, spaceMinCapacity,
                    spaceMaxCapacity, proposedPartySize, slotStart);
            }
        }

        return true;
    }

    private boolean isWithinOperatingHours(LocalDateTime rsvtStart, LocalDateTime rsvtEnd, LocalTime restStart,
        LocalTime restEnd) {
        // normal same-day hours (e.g. 09:00 - 17:00)
        if (!restStart.isAfter(restEnd)) {
            return rsvtStart.toLocalDate().equals(rsvtEnd.toLocalDate()) && !rsvtStart.toLocalTime().isBefore(restStart)
                && !rsvtEnd.toLocalTime().isAfter(restEnd);
        }
        // overnight hours (e.g. 18:00 - 02:00)
        if (!rsvtStart.toLocalTime().isBefore(restStart)) {
            // restaurant opens the same day as reservation starts
            LocalDateTime restStartDt = LocalDateTime.of(rsvtStart.toLocalDate(), restStart);
            LocalDateTime restEndDt = LocalDateTime.of(rsvtStart.toLocalDate().plusDays(1), restEnd);
            return !rsvtStart.isBefore(restStartDt) && !rsvtEnd.isAfter(restEndDt);
        } else {
            // restaurant opened the day before reservation starts
            LocalDateTime restStartDt = LocalDateTime.of(rsvtStart.toLocalDate().minusDays(1), restStart);
            LocalDateTime restEndDt = LocalDateTime.of(rsvtStart.toLocalDate(), restEnd);
            return !rsvtStart.isBefore(restStartDt) && !rsvtEnd.isAfter(restEndDt);
        }
    }

    public boolean deleteReservation(ObjectId id) {
        Optional<Reservation> existingReservation = reservationRepository.findById(id);
        if (existingReservation.isPresent()) {
            reservationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Reservation> getReservationsByRestaurant(ObjectId restaurantId) {
        return reservationRepository.findAll().stream()
            .filter(reservation -> reservation.getRestaurantId().equals(restaurantId))
            .toList();
    }

    public List<Reservation> getReservationsBySpace(ObjectId restaurantId, UUID spaceId) {
        return reservationRepository.findAll().stream()
            .filter(reservation -> reservation.getRestaurantId().equals(restaurantId) &&
                reservation.getSpaceId().equals(spaceId))
            .toList();
    }
}