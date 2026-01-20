# private-dining-take-home-java

## Project Overview

This project enhances the Private Dining Reservation API by introducing:
1. Slot-based reservation availability with flexible capacity management
2. Occupancy analytics reporting over a configurable time rangeProject Overview

## Scope & Assumptions

- Each restaurants has its own operationing hours, which can be overnight (e.g. 10pm-2am the next day)
- Operating hours are consistent across all spaces. There's no seperate operating hours for spaces at the moment (e.g. dining area vs bars)
- Operating hours remain fixed to a particular restaurant, holiday hours/day-to-day time is not supported
- Only a single timezone `ZoneId.systemDefault()` is supported (customisable)
- A time slot of 30 minutes is adopted (customisable)
- Min/max space capacity should be obeyed at any time, which means a proposed reservation will be rejected if there exists any time slot violating the min/max capacity rule
- A valid reservation should be shorter than 24 hours
- All space IDs and restaurant IDs are unique across the system
- The `capacity` of a restaurant is not used in this project, whilst only the sum of the max capacity of all spaces in a restaurants are calculated as its capacity of private dining in the occupancy report
- The standard date & time format of the system is *dd-MM-yyyy HH:mm:ss*
- Reservations and reports are only allowed to be requested in half-hour increments (e.g. 10:00, 10:30, 11:00, etc.)

## High-Level Design

See: *docs/design.md*

## Design Decisions

- Business rules are evaluated in local date time and persisted in UTC. Restaurant operating hours are evaluated in local time and persisted in String. See: *docs/adr/utc-time-handling.md*
- Availability is calculated using fixed time-block slots. See: *docs/adr/fixed-time-slots.md*
- Multiple overlapping reservations are allowed as long as capacity constraints are respected. See: *docs/adr/flexible-capacity-management.md*
- MongoDB is adopted in the system. See: *docs/adr/db.md*
- Optimistic MongoDB locks and `ReentrantLock`s are adopted to handle inter- & intra-JVMs concurrency. See: *docs/adr/concurrency.md*
- A space-wise occupancy report will be returned if a valid space ID is provided, while a restaurant-wise overall report will be returned otherwise. See: *docs/adr/occupancy-reporting.md*

## API Overview

Note: only added/updated APIs are documented here.

- **POST /v1/reservations**: Create a new reservation in the system
  - Path & query params: N/A
  - Request Body:
    - ReservationDTO
      - id
      - restaurantId
      - spaceId
      - customerEmail
      - startTime: 
      - endTime
      - partySize
      - status
  - Response:
    - 201: Reservation created successfully
    - 400: Invalid party size for the space capacity/Invalid reservation
    - 404: Restaurant or space not found
    - 409: Reservation time slot conflicts with existing reservation
- **GET /v1/reporting/{id}/occupancy**: Retrieve occupancy levels for a given restaurant over a specified period
  - Path param:
    - id: restaurant ID
  - Query param:
    - start (requied): Report Start time (Must in the format of "yyyy-MM-dd’T’HH:mm:ss")
    - end (requied): Report End time (Must in the format of "yyyy-MM-dd’T’HH:mm:ss")
    - spaceId (optional): space ID
  - Response:
    - 200: Valid id and date/time range
    - 404: Restaurant/space not found
    - 400: Invalid start/end time

## Concurrency & Data Integrity

All operations involving multiple repository calls are executed within a single transaction.
Overlapping reservations are queried and validated before persistence to prevent overbooking
under concurrent requests.

## Prerequisites

- Java
- Maven
- SpringBoot
- MongoDB (embedded)

## Running the project

### Run by CMD

The project can be started using `mvn spring-boot:run`, and runs with an embedded instance of MongoDB

### Run in IDE

The application is able to run in an IDE. Simply import the project and run as an ordinary SpringBoot project.

### Run with JAR

Run the following command to run the application. Please replace the `version` yourself. 

```bash
java -jar private-dining-<version>.jar
```

Note: 

- DB Host: localhost:27017
- DB Name: private_dining

## Testing

All added/updated codes are well-tested with sufficient test cases. Current line coverages:

- `/controller`: 100% (66/66)
- `/service`: 91% (197/215)

## Future Improvements

- Multiple timezones
- Flexible operating hours
- Space-wise operating hours
- Customisable occupancy report request granularity (currently a fixed 30-minute slot may be too small for a large time range)
- Observability improvements (e.g. metrics, tracing exporters)

## AI Tooling Disclosure

AI tools were used as a supplementary aid for design validation, documentation and implementation references only.
All implementation code and architectural decisions were made and authored by the applicant.
