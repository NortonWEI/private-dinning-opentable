package com.opentable.privatedining.service;

import com.opentable.privatedining.common.Constant;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class ReportingService {

    private final RestaurantService restaurantService;
    private final ReservationService reservationService;
    private final OccupancyReportMapper occupancyReportMapper;

    public ReportingService(RestaurantService restaurantService, ReservationService reservationService,
        OccupancyReportMapper occupancyReportMapper) {
        this.restaurantService = restaurantService;
        this.reservationService = reservationService;
        this.occupancyReportMapper = occupancyReportMapper;
    }

    public Optional<OccupancyReport> getOccupancyReport(OccupancyReport request) {
        validateParams(request);

        validateExistence(request);

        return getReport(request);
    }

    private Optional<OccupancyReport> getReport(OccupancyReport request) {
        ObjectId restaurantId = request.getRestaurantId();
        UUID spaceId = request.getSpaceId();
        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();
        Long granularity = request.getGranularity();

        long minuteInterval = Duration.between(start, end).toMinutes();

        // get the ceiling value of (minuteInterval / granularity)
        long limit = (minuteInterval + granularity - 1) / granularity;
        // cast limit to int, throw exception if overflow
        // this is unlikely to happen in practice since it would require a very large time range, just being defensive
        int intLimit;
        try {
            intLimit = Math.toIntExact(limit);
        } catch (ArithmeticException e) {
            throw new InvalidReportingException("given time range is too large");
        }

        OccupancyReport response = occupancyReportMapper.copy(request);

        if (request.getSpaceId() == null) {
            // return restaurant-wide occupancy data with each space's occupancy included
            List<Reservation> reservations = reservationService.getReservationByRestaurantAndOverlap(restaurantId,
                start, end);
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .get(); // validated existence earlier
            List<OccupancyPoint> restaurantPoints = new ArrayList<>(intLimit);
            response.setSpaceData(new ArrayList<>(restaurant.getSpaces().size()));
            for (Space space : restaurant.getSpaces()) {
                List<Reservation> reservationsBySpace = reservations.stream()
                    .filter(r -> r.getSpaceId().equals(space.getId()))
                    .toList();
                populateSpacePoint(request, space, intLimit, granularity, reservationsBySpace, response);
            }
            populateRestaurantPoint(request, intLimit, granularity, response, restaurantPoints, restaurantId,
                restaurant);
        } else {
            // return space-specific occupancy data
            List<Reservation> reservations = reservationService.getReservationByRestaurantAndSpaceAndOverlap(
                restaurantId, spaceId, start, end);
            Space space = restaurantService.getSpaceById(restaurantId, spaceId).get(); // validated existence earlier
            response.setSpaceData(new ArrayList<>(1));
            populateSpacePoint(request, space, intLimit, granularity, reservations, response);
        }

        return Optional.of(response);
    }

    private void populateRestaurantPoint(OccupancyReport request, int intLimit, Long granularity,
        OccupancyReport response, List<OccupancyPoint> restaurantPoints, ObjectId restaurantId, Restaurant restaurant) {
        int totalCapacity = restaurant.getSpaces().stream()
            .mapToInt(Space::getMaxCapacity)
            .sum();
        for (int i = 0; i < intLimit; i++) {
            LocalDateTime slotStart = request.getStart().plusMinutes(i * granularity);
            LocalDateTime slotEnd = slotStart.plusMinutes(granularity);
            int finalI = i;
            int totalOccupancy = response.getSpaceData().stream()
                .mapToInt(spaceData -> spaceData.getPoints().get(finalI).getOccupancy())
                .sum();
            double occupancyRate = totalCapacity == 0 ? 0d : (double) totalOccupancy / totalCapacity;
            restaurantPoints.add(new OccupancyPoint(slotStart, slotEnd, totalCapacity, totalOccupancy,
                occupancyRate));
        }
        response.setRestaurantData(
            new OccupancyData(restaurantId.toHexString(), restaurant.getName(), restaurantPoints));
    }

    private void populateSpacePoint(OccupancyReport request, Space space, int intLimit, Long granularity,
        List<Reservation> reservationsBySpace, OccupancyReport response) {
        OccupancyData occupancyDataBySpace = new OccupancyData(space.getId().toString(), space.getName(),
            new ArrayList<>(intLimit));
        for (int i = 0; i < intLimit; i++) {
            LocalDateTime slotStart = request.getStart().plusMinutes(i * granularity);
            LocalDateTime slotEnd = slotStart.plusMinutes(granularity);
            int totalOccupancy = reservationsBySpace.stream()
                .filter(r -> r.getStartTime().isBefore(slotEnd) && r.getEndTime().isAfter(slotStart))
                .mapToInt(Reservation::getPartySize)
                .sum();

            OccupancyPoint point = new OccupancyPoint(slotStart, slotEnd, space.getMaxCapacity(),
                totalOccupancy,
                space.getMaxCapacity() == 0 ? 0d
                    : (double) totalOccupancy / space.getMaxCapacity());
            occupancyDataBySpace.getPoints().add(point);
        }
        response.getSpaceData().add(occupancyDataBySpace);
    }

    private void validateExistence(OccupancyReport request) {
        // Validate that the restaurant/space exists
        ObjectId restaurantId = request.getRestaurantId();
        Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            throw new RestaurantNotFoundException(restaurantId);
        }

        if (request.getSpaceId() != null) {
            Restaurant restaurant = restaurantOpt.get();
            // Assume space IDs are unique within a restaurant
            Map<UUID, Space> idToSpace = restaurant.getSpaces().stream()
                .collect(Collectors.toMap(Space::getId, s -> s, (s1, s2) -> s1));
            UUID spaceId = request.getSpaceId();
            Optional<Space> optSpace = Optional.ofNullable(idToSpace.get(spaceId));
            if (optSpace.isEmpty()) {
                throw new SpaceNotFoundException(restaurantId, spaceId);
            }
        }
    }

    private void validateParams(OccupancyReport request) {
        // Validation null checks
        if (request.getRestaurantId() == null || request.getStart() == null || request.getEnd() == null
            || request.getGranularity() == null) {
            throw new InvalidReportingException("parameters must not be null");
        }

        // Validation start before end
        if (!request.getStart().isBefore(request.getEnd())) {
            throw new InvalidReportingException("start time must be before end time");
        }

        // Validation time block alignment
        if (request.getStart().getMinute() % Constant.BLOCK_INTERVAL != 0
            || request.getEnd().getMinute() % Constant.BLOCK_INTERVAL != 0) {
            throw new InvalidReportingException("start and end times must be half-hour or hourly aligned");
        }

        // Validation granularity
        if (request.getGranularity() <= 0 || request.getGranularity() % Constant.BLOCK_INTERVAL != 0) {
            throw new InvalidReportingException(
                "granularity must be positive and divisible by " + Constant.BLOCK_INTERVAL);
        }
    }
}
