package com.opentable.privatedining.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.opentable.privatedining.TestDataHelper;
import com.opentable.privatedining.exception.InvalidReportingException;
import com.opentable.privatedining.exception.RestaurantNotFoundException;
import com.opentable.privatedining.exception.SpaceNotFoundException;
import com.opentable.privatedining.mapper.reporting.OccupancyReportMapper;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.model.reporting.OccupancyData;
import com.opentable.privatedining.model.reporting.OccupancyPoint;
import com.opentable.privatedining.model.reporting.OccupancyReport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReportingServiceTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private OccupancyReportMapper occupancyReportMapper;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReportingService reportingService;

    private static Stream<Arguments> nullRequests() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 30, 12, 0);

        OccupancyReport missingRestaurant = new OccupancyReport();
        missingRestaurant.setRestaurantId(null);
        missingRestaurant.setStart(start);
        missingRestaurant.setEnd(start.plusHours(1));

        OccupancyReport missingStart = new OccupancyReport();
        missingStart.setRestaurantId(new ObjectId());
        missingStart.setStart(null);
        missingStart.setEnd(start.plusHours(1));

        OccupancyReport missingEnd = new OccupancyReport();
        missingEnd.setRestaurantId(new ObjectId());
        missingEnd.setStart(start);
        missingEnd.setEnd(null);

        return Stream.of(Arguments.of(missingRestaurant), Arguments.of(missingStart), Arguments.of(missingEnd));
    }

    @ParameterizedTest
    @MethodSource("nullRequests")
    void getOccupancyReport_WhenNullValueInRequest_ShouldThrowException(OccupancyReport request) {
        // When & Then
        assertThrows(InvalidReportingException.class, () -> reportingService.getOccupancyReport(request));
    }

    @Test
    void getOccupancyReport_WhenEndBeforeStart_ShouldThrowException() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 30, 12, 0);

        OccupancyReport request = new OccupancyReport();
        request.setRestaurantId(new ObjectId());
        request.setStart(start);
        request.setEnd(start.minusHours(1));

        // When & Then
        assertThrows(InvalidReportingException.class, () -> reportingService.getOccupancyReport(request));
    }

    @Test
    void getOccupancyReport_WhenTimeNotAlignedInBlock_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        LocalDateTime start = LocalDateTime.of(2026, 1, 30, 12, 1);

        OccupancyReport request = new OccupancyReport();
        request.setRestaurantId(restaurantId);
        request.setStart(start);
        request.setEnd(start.plusHours(1));

        // When & Then
        assertThrows(InvalidReportingException.class, () -> reportingService.getOccupancyReport(request));
    }

    @Test
    void getOccupancyReport_WhenRestaurantNotFound_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        OccupancyReport request = createTestRequest(restaurantId);

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RestaurantNotFoundException.class, () -> reportingService.getOccupancyReport(request));
        verify(restaurantService).getRestaurantById(restaurantId);
    }

    @Test
    void getOccupancyReport_WhenSpaceNotFound_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        OccupancyReport request = createTestRequest(restaurantId);
        request.setSpaceId(spaceId);

        Restaurant restaurant = TestDataHelper.createTestRestaurant();
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(SpaceNotFoundException.class, () -> reportingService.getOccupancyReport(request));
        verify(restaurantService).getRestaurantById(restaurantId);
    }

    @Test
    void getOccupancyReport_WhenTimeRangeTooLarge_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        LocalDateTime start = LocalDateTime.of(1900, 1, 30, 12, 0);

        Space testSpace = new Space("Test Space", 10, 100);

        OccupancyReport request = new OccupancyReport();
        request.setRestaurantId(restaurantId);
        request.setStart(start);
        request.setEnd(start.plusYears(100000000));
        request.setSpaceId(testSpace.getId());

        Restaurant restaurant = TestDataHelper.createTestRestaurant();
        restaurant.setSpaces(List.of(testSpace));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(InvalidReportingException.class, () -> reportingService.getOccupancyReport(request));
    }

    @Test
    void getOccupancyReport_NullSpaceId_ShouldReturnRestaurantOccupancyReport() {
        // Given
        ObjectId restaurantId = new ObjectId();

        Space testSpace1 = new Space("Test Space 1", 10, 100);
        Space testSpace2 = new Space("Test Space 2", 30, 300);

        OccupancyReport request = createTestRequest(restaurantId);

        Restaurant restaurant = TestDataHelper.createTestRestaurant();
        restaurant.setSpaces(List.of(testSpace1, testSpace2));

        Reservation reservation1 = TestDataHelper.createTestReservation("customer@example.com", 50);
        Reservation reservation2 = TestDataHelper.createTestReservation("another@example.com", 20);
        reservation1.setSpaceId(testSpace1.getId());
        reservation2.setSpaceId(testSpace1.getId());

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(occupancyReportMapper.copy(request)).thenReturn(request);
        when(reservationService.getReservationByRestaurantAndOverlap(any(), any(), any())).thenReturn(
            List.of(reservation1, reservation2));

        // When & Then
        Optional<OccupancyReport> actual = reportingService.getOccupancyReport(request);
        verify(reservationService).getReservationByRestaurantAndOverlap(restaurantId, request.getStart(),
            request.getEnd());
        verify(restaurantService, times(2)).getRestaurantById(restaurantId);

        OccupancyReport expected = new OccupancyReport(restaurantId, null, request.getStart(),
            request.getEnd(), new OccupancyData(restaurantId.toHexString(), restaurant.getName(),
            List.of(
                new OccupancyPoint(request.getStart(), request.getStart().plusMinutes(30), 400, 70, 0.175d),
                new OccupancyPoint(request.getStart().plusMinutes(30), request.getStart().plusMinutes(60), 400,
                    0, 0d))
        ),
            List.of(
                new OccupancyData(testSpace1.getId().toString(), testSpace1.getName(),
                    List.of(new OccupancyPoint(request.getStart(), request.getStart().plusMinutes(30), 100, 70, 0.7d),
                        new OccupancyPoint(request.getStart().plusMinutes(30), request.getStart().plusMinutes(60), 100,
                            0, 0d))),
                new OccupancyData(testSpace2.getId().toString(), testSpace2.getName(),
                    List.of(new OccupancyPoint(request.getStart(), request.getStart().plusMinutes(30), 300, 0, 0d),
                        new OccupancyPoint(request.getStart().plusMinutes(30), request.getStart().plusMinutes(60), 300,
                            0, 0d))))
        );

        assertThat(actual).isPresent();
        assertThat(actual.get()).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void getOccupancyReport_ValidSpaceId_ShouldReturnSpaceOccupancyReport() {
        // Given
        ObjectId restaurantId = new ObjectId();

        Space testSpace1 = new Space("Test Space 1", 10, 100);
        Space testSpace2 = new Space("Test Space 2", 30, 300);

        OccupancyReport request = createTestRequest(restaurantId);
        request.setSpaceId(testSpace1.getId());

        Restaurant restaurant = TestDataHelper.createTestRestaurant();
        restaurant.setSpaces(List.of(testSpace1, testSpace2));

        Reservation reservation1 = TestDataHelper.createTestReservation("customer@example.com", 50);
        Reservation reservation2 = TestDataHelper.createTestReservation("another@example.com", 20);
        reservation1.setSpaceId(testSpace1.getId());
        reservation2.setSpaceId(testSpace1.getId());

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(occupancyReportMapper.copy(request)).thenReturn(request);
        when(reservationService.getReservationByRestaurantAndSpaceAndOverlap(any(), any(), any(), any())).thenReturn(
            List.of(reservation1, reservation2));
        when(restaurantService.getSpaceById(any(), any())).thenReturn(Optional.of(testSpace1));

        // When & Then
        Optional<OccupancyReport> actual = reportingService.getOccupancyReport(request);
        verify(reservationService).getReservationByRestaurantAndSpaceAndOverlap(restaurantId, testSpace1.getId(),
            request.getStart(), request.getEnd());
        verify(restaurantService).getSpaceById(restaurantId, testSpace1.getId());

        OccupancyReport expected = new OccupancyReport(restaurantId, testSpace1.getId(), request.getStart(),
            request.getEnd(), null,
            List.of(new OccupancyData(testSpace1.getId().toString(), testSpace1.getName(),
                List.of(new OccupancyPoint(request.getStart(), request.getStart().plusMinutes(30), 100, 70, 0.7d),
                    new OccupancyPoint(request.getStart().plusMinutes(30), request.getStart().plusMinutes(60), 100, 0,
                        0d)))));

        assertThat(actual).isPresent();
        assertThat(actual.get()).usingRecursiveComparison().isEqualTo(expected);
    }

    private OccupancyReport createTestRequest(ObjectId restaurantId) {
        LocalDateTime start = LocalDateTime.of(2026, 1, 30, 21, 30);

        OccupancyReport request = new OccupancyReport();
        request.setRestaurantId(restaurantId);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        return request;
    }
}
