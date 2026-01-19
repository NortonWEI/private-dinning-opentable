package com.opentable.privatedining.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void getAllReservations_ShouldReturnAllReservations() {
        // Given
        Reservation reservation1 = createTestReservation("customer1@example.com", 4);
        Reservation reservation2 = createTestReservation("customer2@example.com", 6);
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(reservationRepository.findAll()).thenReturn(reservations);

        // When
        List<Reservation> result = reservationService.getAllReservations();

        // Then
        assertEquals(2, result.size());
        assertThat(result).containsExactlyInAnyOrder(reservation1, reservation2);
        verify(reservationRepository).findAll();
    }

    @Test
    void getReservationById_WhenReservationExists_ShouldReturnReservation() {
        // Given
        ObjectId reservationId = new ObjectId();
        Reservation reservation = createTestReservation("test@example.com", 4);
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When
        Optional<Reservation> result = reservationService.getReservationById(reservationId);

        // Then
        assertTrue(result.isPresent());
        assertThat(result.get()).isEqualTo(reservation);
        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void getReservationById_WhenReservationNotFound_ShouldReturnEmpty() {
        // Given
        ObjectId reservationId = new ObjectId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When
        Optional<Reservation> result = reservationService.getReservationById(reservationId);

        // Then
        assertFalse(result.isPresent());
        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void createReservation_WhenEndsEarlierReservation_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation invalidReservation = createTestReservation("customer@example.com", 4);
        invalidReservation.setEndTime(invalidReservation.getStartTime().minusHours(1));
        invalidReservation.setRestaurantId(restaurantId);
        invalidReservation.setSpaceId(spaceId);

        Reservation savedReservation = createTestReservation("customer@example.com", 4);
        savedReservation.setId(new ObjectId());

        // Then
        assertThrows(InvalidReservationException.class, () -> reservationService.createReservation(invalidReservation));
    }

    @Test
    void createReservation_WhenStartsBeforeNowReservation_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation invalidReservation = createTestReservation("customer@example.com", 4);
        invalidReservation.setEndTime(LocalDateTime.now().minusHours(1));
        invalidReservation.setRestaurantId(restaurantId);
        invalidReservation.setSpaceId(spaceId);

        Reservation savedReservation = createTestReservation("customer@example.com", 4);
        savedReservation.setId(new ObjectId());

        // Then
        assertThrows(InvalidReservationException.class, () -> reservationService.createReservation(invalidReservation));
    }

    @Test
    void createReservation_WhenTooLongReservation_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation invalidReservation = createTestReservation("customer@example.com", 4);
        invalidReservation.setEndTime(invalidReservation.getStartTime().plusHours(25));
        invalidReservation.setRestaurantId(restaurantId);
        invalidReservation.setSpaceId(spaceId);

        Reservation savedReservation = createTestReservation("customer@example.com", 4);
        savedReservation.setId(new ObjectId());

        // Then
        assertThrows(InvalidReservationException.class, () -> reservationService.createReservation(invalidReservation));
    }

    @Test
    void createReservation_WhenNotInBlockReservation_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation invalidReservation = createTestReservation("customer@example.com", 4);
        invalidReservation.setStartTime(invalidReservation.getStartTime().plusMinutes(1));
        invalidReservation.setRestaurantId(restaurantId);
        invalidReservation.setSpaceId(spaceId);

        Reservation savedReservation = createTestReservation("customer@example.com", 4);
        savedReservation.setId(new ObjectId());

        // Then
        assertThrows(InvalidReservationException.class, () -> reservationService.createReservation(invalidReservation));
    }

    @Test
    void createReservation_WhenValidReservation_ShouldReturnSavedReservation() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        Restaurant restaurant = createTestRestaurant();
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        Reservation savedReservation = createTestReservation("customer@example.com", 4);
        savedReservation.setId(new ObjectId());

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndSpaceIdAndOverlap(any(), any(), any(), any())).thenReturn(
            Arrays.asList());
        when(reservationRepository.save(reservation)).thenReturn(savedReservation);

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertThat(result).isEqualTo(savedReservation);
        verify(restaurantService).getRestaurantById(restaurantId);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void createReservation_WhenRestaurantNotFound_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RestaurantNotFoundException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(restaurantService).getRestaurantById(restaurantId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenSpaceNotFound_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        Restaurant restaurant = createTestRestaurant();

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(SpaceNotFoundException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(restaurantService).getRestaurantById(restaurantId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenOutsideSameDayOperatingHours_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setStartTime(LocalDateTime.of(2026, 1, 30, 9, 0)); // Before opening time
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        Restaurant restaurant = createTestRestaurant();
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        // When
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // Then
        assertThrows(InvalidReservationException.class, () -> reservationService.createReservation(reservation));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenOutsideOvernightOperatingHours_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setEndTime(LocalDateTime.of(2026, 1, 31, 3, 0)); // After closing time
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        Restaurant restaurant = createTestRestaurant();
        restaurant.setEndTime(LocalTime.of(2, 0)); // Overnight hours
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        // When
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // Then
        assertThrows(InvalidReservationException.class, () -> reservationService.createReservation(reservation));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenPartySizeBelowMinCapacity_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 1); // Below min capacity
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        Restaurant restaurant = createTestRestaurant();
        Space space = new Space("Test Space", 2, 8); // Min capacity is 2
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        // When
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // Then
        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(reservation));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenPartySizeAboveMaxCapacity_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 10); // Above max capacity
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        Restaurant restaurant = createTestRestaurant();
        Space space = new Space("Test Space", 2, 8); // Max capacity is 8
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        // When
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // Then
        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(reservation));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenConcurrentReservationsExceedMaxExists_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        Reservation newReservation = createTestReservation("customer@example.com", 6);
        newReservation.setRestaurantId(restaurantId);
        newReservation.setSpaceId(spaceId);

        // Existing reservation
        Reservation existingReservation1 = createTestReservation("other@example.com", 2);
        existingReservation1.setRestaurantId(restaurantId);
        existingReservation1.setSpaceId(spaceId);
        existingReservation1.setStartTime(newReservation.getStartTime().plusMinutes(30));
        existingReservation1.setEndTime(newReservation.getEndTime().plusMinutes(30));

        Reservation existingReservation2 = createTestReservation("other@example.com", 2);
        existingReservation2.setRestaurantId(restaurantId);
        existingReservation2.setSpaceId(spaceId);
        existingReservation2.setStartTime(newReservation.getStartTime().minusMinutes(30));
        existingReservation2.setEndTime(newReservation.getEndTime().minusMinutes(30));

        Restaurant restaurant = createTestRestaurant();
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        // When
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndSpaceIdAndOverlap(any(), any(), any(), any())).thenReturn(
            Arrays.asList(existingReservation1, existingReservation2));

        // Then
        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(newReservation));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenNewReservationExceedsMinExists_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        Reservation newReservation = createTestReservation("customer@example.com", 1);
        newReservation.setRestaurantId(restaurantId);
        newReservation.setSpaceId(spaceId);

        // Existing reservation
        Reservation existingReservation = createTestReservation("other@example.com", 6);
        existingReservation.setRestaurantId(restaurantId);
        existingReservation.setSpaceId(spaceId);
        existingReservation.setStartTime(newReservation.getStartTime().minusMinutes(30));
        existingReservation.setEndTime(newReservation.getEndTime().minusMinutes(30));

        Restaurant restaurant = createTestRestaurant();
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        // When
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndSpaceIdAndOverlap(any(), any(), any(), any())).thenReturn(
            Arrays.asList(existingReservation));

        // Then
        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(newReservation));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenConcurrentReservationsExist_ShouldReturnSavedReservation() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        Reservation newReservation = createTestReservation("customer@example.com", 2);
        newReservation.setRestaurantId(restaurantId);
        newReservation.setSpaceId(spaceId);

        // Existing reservation
        Reservation existingReservation = createTestReservation("other@example.com", 4);
        existingReservation.setRestaurantId(restaurantId);
        existingReservation.setSpaceId(spaceId);
        existingReservation.setStartTime(newReservation.getStartTime().minusMinutes(30));
        existingReservation.setEndTime(newReservation.getEndTime().minusMinutes(30));

        Restaurant restaurant = createTestRestaurant();
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        Reservation savedReservation = createTestReservation("customer@example.com", 2);
        savedReservation.setId(new ObjectId());

        // When
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndSpaceIdAndOverlap(any(), any(), any(), any())).thenReturn(
            Arrays.asList(existingReservation));
        when(reservationRepository.save(newReservation)).thenReturn(savedReservation);

        // When
        Reservation result = reservationService.createReservation(newReservation);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertThat(result).isEqualTo(savedReservation);
        verify(restaurantService).getRestaurantById(restaurantId);
        verify(reservationRepository).save(newReservation);
    }

    @Test
    void deleteReservation_WhenReservationExists_ShouldReturnTrue() {
        // Given
        ObjectId reservationId = new ObjectId();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When
        boolean result = reservationService.deleteReservation(reservationId);

        // Then
        assertTrue(result);
        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    void deleteReservation_WhenReservationNotFound_ShouldReturnFalse() {
        // Given
        ObjectId reservationId = new ObjectId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When
        boolean result = reservationService.deleteReservation(reservationId);

        // Then
        assertFalse(result);
        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).deleteById(reservationId);
    }

    @Test
    void getReservationsByRestaurant_ShouldReturnFilteredReservations() {
        // Given
        ObjectId restaurantId = new ObjectId();
        ObjectId otherRestaurantId = new ObjectId();

        Reservation reservation1 = createTestReservation("customer1@example.com", 4);
        reservation1.setRestaurantId(restaurantId);

        Reservation reservation2 = createTestReservation("customer2@example.com", 6);
        reservation2.setRestaurantId(otherRestaurantId);

        Reservation reservation3 = createTestReservation("customer3@example.com", 2);
        reservation3.setRestaurantId(restaurantId);

        List<Reservation> allReservations = Arrays.asList(reservation1, reservation2, reservation3);

        when(reservationRepository.findAll()).thenReturn(allReservations);

        // When
        List<Reservation> result = reservationService.getReservationsByRestaurant(restaurantId);

        // Then
        assertEquals(2, result.size());
        assertThat(result).containsExactlyInAnyOrder(reservation1, reservation3);
        verify(reservationRepository).findAll();
    }

    @Test
    void getReservationsBySpace_ShouldReturnFilteredReservations() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        UUID otherSpaceId = UUID.randomUUID();

        Reservation reservation1 = createTestReservation("customer1@example.com", 4);
        reservation1.setRestaurantId(restaurantId);
        reservation1.setSpaceId(spaceId);

        Reservation reservation2 = createTestReservation("customer2@example.com", 6);
        reservation2.setRestaurantId(restaurantId);
        reservation2.setSpaceId(otherSpaceId);

        Reservation reservation3 = createTestReservation("customer3@example.com", 2);
        reservation3.setRestaurantId(restaurantId);
        reservation3.setSpaceId(spaceId);

        List<Reservation> allReservations = Arrays.asList(reservation1, reservation2, reservation3);

        when(reservationRepository.findAll()).thenReturn(allReservations);

        // When
        List<Reservation> result = reservationService.getReservationsBySpace(restaurantId, spaceId);

        // Then
        assertEquals(2, result.size());
        assertThat(result).containsExactlyInAnyOrder(reservation1, reservation3);
        verify(reservationRepository).findAll();
    }

    private Reservation createTestReservation(String customerEmail, int partySize) {
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

    private Restaurant createTestRestaurant() {
        return new Restaurant(
            "Test Restaurant", "Address", "Cuisine", 50, LocalTime.of(11, 0), LocalTime.of(23, 0));
    }
}