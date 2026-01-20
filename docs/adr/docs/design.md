# Private Dining Reservation System V2 – Design Document

## Overview

This document describes the high-level design of the Private Dining Reservation System V2.
The system enhances an existing reservation API by introducing slot-based availability,
flexible capacity management, and occupancy analytics. A dedicated occupancy reporting API is also implemented.

The primary goals of the design are correctness, clarity of business rules, and extensibility,
while keeping the implementation simple and focused on the problem statement.

---

## Scope and Assumptions

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

---

## High-Level Architecture

```text
Client
  ↓
REST Controllers (DTOs)
  ↓
Service Layer (Models)
  ↓
Persistence Layer (MongoDB)
```

### Responsibilities

**Controllers**

- Handle HTTP requests and responses
- Perform basic request validation
- Delegate business logic to services

**Service Layer**

- Validate the input parameters
- Normalise reservations into time slots
- Enforce minimum and maximum capacity rules
- Create reservations atomically
- Aggregate occupancy metrics for users

**Persistence Layer**

- Store reservations, restaurant and space configuration
- Support efficient overlap queries using UTC timestamps

## Core Domain Model

- Restaurant
  - id: ObjectId
  - name: String
  - address: String
  - cuisineType: String
  - capacity: Integer
  - **startTime: LocalTime**
  - **endTime: LocalTime**
- Space
  - id: UUID
  - name: String
  - minCapacity: Integer
  - maxCapacity: Integer
- Reservation
  - id: ObjectId
  - restaurantId: ObejctId
  - spaceId: UUID
  - customerEmail: String
  - startTime: LocalDateTime
  - endTime: LocalDateTime
  - partySize: Integer
  - status: String
- OccupancyReport
  - restaurantId: ObejctId
  - spaceId: UUID
  - start: LocalDateTime
  - end: LocalDateTime
  - granularity: Long
  - restaurantData: OccupancyData
  - spaceData: List<OccupancyData>
- OccupancyData
  - id: String
  - name: String
  - points: List<OccupancyPoint>
- OccupancyPoint
  - slotStart: LocalDateTime
  - slotEnd: LocalDateTime
  - capacity: Integer
  - occupancy: Integer
  - occupancyRate: Double

## Feature 1: Reservation Availability and Capacity Management

### Reservation Creation Flow

1. Receive reservation request
2. Raise ReentrantLock for concurrency handling
3. Validation (null check, duration check, block period check, existence check, operating hour check, existing reservation conflict check, etc.)
4. Existing reservation conflict check
   1. Fetch overlapping concurrent reservations
   2. Aggregate headcount per slot
   3. Reject if capacity limits are exceeded in any slot
5. Persist reservation in UTC
6. Drop ReentrantLock

### Slot Optimisation

The day is divided into fixed time-block slots (30 minutes). Capacity is evaluated per slot
rather than per reservation, allowing multiple overlapping bookings while preventing
overbooking.

### Reservation Creation

- Minimum capacity ensures operational efficiency
- Maximum capacity is enforced per slot
- Concurrent reservations are allowed as long as total headcount remains within limits

## Feature 2: Occupancy Analytics

### Reporting Flow

1. Generate slots for the requested date/time range
2. Query reservations overlapping the range
3. Aggregate party size per slot as total occupancy
4. Calculate utilisation metrics (occupancy rate = total occupied headcount / total capacity)

### Reporting Output

The API returns time-series occupancy data per slot, including:

- Occupied headcount
- Maximum capacity
- Utilization percentage (aka. occupancy rate)

Each data is returned as a data point (OccupancyPoint, reused by both restaurants and spaces). This structure enables easy visualisation and trend analysis for frontend utilisation.

## Concurrency Considerations

To prevent overbooking under concurrent requests:

- Capacity checks and reservation creation are executed atomically
- Overlapping reservations are queried before persistence
- UTC timestamps ensure consistent overlap evaluation

## Scalability Considerations

- Slot-based availability enables efficient aggregation and reporting
- UTC time supports fast and efficient range queries
- Occupancy analytics can be pre-aggregated or cached if traffic increases
- The concurrency handling (JPA optimistic locking + in‑JVM locking) allows future scale-up in no time, both horizontally and vertically.

## Related Documentation

ADRs are located in: */docs/adr*