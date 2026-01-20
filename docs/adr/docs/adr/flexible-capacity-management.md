# ADR-Flexible-Capacity-Management

## Status

Accepted

## Context

The system uses fixed 30-minute slots and evaluates business rules in restaurant local time. Multiple reservations may overlap in time for the same space; the system must avoid overbooking while maximising utilization. Space capacity constraints (min/max) and slot boundaries are enforced.

## Decision

Allow overlapping concurrent reservations for the same space as long as aggregated occupancy per slot remains within capacity constraints. Enforce the following checks atomically at reservation creation:

1. Validate request alignment and duration (half-hour boundaries, < 24 hours).
2. Validate reservation party size against the space's per-reservation limits (partySize between space.minCapacity and space.maxCapacity).
3. Normalize the reservation into the set of covered closed-open slots [slotStart, slotEnd) in restaurant local time, convert each slot boundary to UTC instants for persistence/query.
4. For each affected slot: Query existing reservations overlapping that slot and sum their party sizes. Compute projected occupancy = existingSum + newReservation.partySize. Reject the reservation if projected occupancy > space.maxCapacity.
5. Persist the reservation (UTC timestamps) only when all slot checks pass.

## Rationale

- Slot bucketing and per-slot aggregation enable simple, deterministic capacity checks while permitting overlapping bookings that fit capacity.
- This approach improves utilization vs. exclusive time blocking and keeps business rules straightforward to implement and reason about.

## Consequences

Positive:

- Higher table/space utilisation by allowing safe overlap.

- Deterministic capacity enforcement per discrete slot.

Negative:

- Reservation acceptance requires querying and aggregating per affected slot, adding query cost proportional to reservation length.
- Concurrency requires careful locking and optimistic checks to avoid transient overbooking under high contention.



## Alternatives (rejected)

- Exclusive time blocking per reservation: simpler checks but wastes capacity and reduces throughput.
- Centralised counters (Redis) per slot: faster under load but adds infra complexity and eventual consistency tradeoffs.