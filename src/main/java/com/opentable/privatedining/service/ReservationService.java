package com.opentable.privatedining.service;

import com.opentable.privatedining.exception.InvalidPartySizeException;
import com.opentable.privatedining.exception.InvalidReservationException;
import com.opentable.privatedining.exception.ReservationConflictException;
import com.opentable.privatedining.exception.RestaurantNotFoundException;
import com.opentable.privatedining.exception.SpaceNotFoundException;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.ReservationRepository;
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
        if (reservation.getStartTime().isAfter(reservation.getEndTime()) ||
            reservation.getStartTime().isEqual(reservation.getEndTime())) {
            throw new InvalidReservationException("Reservation start time must be before end time.");
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

        // Validate party size is within space capacity
        if (reservation.getPartySize() < space.getMinCapacity() ||
            reservation.getPartySize() > space.getMaxCapacity()) {
            throw new InvalidPartySizeException(
                reservation.getPartySize(), space.getMinCapacity(), space.getMaxCapacity());
        }

        // Check for overlapping reservations
        if (hasOverlappingReservation(reservation.getRestaurantId(), reservation.getSpaceId(),
            reservation.getStartTime(), reservation.getEndTime())) {
            throw new ReservationConflictException(
                reservation.getRestaurantId(), reservation.getSpaceId(),
                reservation.getStartTime(), reservation.getEndTime());
        }

        // Check if the reservation is within restaurant operating hours
        if (!isWithinOperatingHours(reservation.getStartTime(), reservation.getEndTime(), restaurant.getStartTime(),
            restaurant.getEndTime())) {
            throw new InvalidReservationException(restaurant.getStartTime(), restaurant.getEndTime(),
                reservation.getStartTime(), reservation.getEndTime());
        }

        return reservationRepository.save(reservation);
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

    private boolean hasOverlappingReservation(ObjectId restaurantId, UUID spaceId,
        LocalDateTime startTime, LocalDateTime endTime) {
        return reservationRepository.findAll().stream()
            .anyMatch(existing -> existing.getRestaurantId().equals(restaurantId) &&
                existing.getSpaceId().equals(spaceId) &&
                isTimeOverlapping(existing.getStartTime(), existing.getEndTime(),
                    startTime, endTime));
    }

    private boolean hasOverlappingReservationExcluding(ObjectId restaurantId, UUID spaceId,
        LocalDateTime startTime, LocalDateTime endTime,
        ObjectId excludeReservationId) {
        return reservationRepository.findAll().stream()
            .anyMatch(existing -> !existing.getId().equals(excludeReservationId) &&
                existing.getRestaurantId().equals(restaurantId) &&
                existing.getSpaceId().equals(spaceId) &&
                isTimeOverlapping(existing.getStartTime(), existing.getEndTime(),
                    startTime, endTime));
    }

    private boolean isTimeOverlapping(LocalDateTime existingStart, LocalDateTime existingEnd,
        LocalDateTime newStart, LocalDateTime newEnd) {
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
}