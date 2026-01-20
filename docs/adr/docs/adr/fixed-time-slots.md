# ADR-Fixed-Time-Slots: Fixed slot availability (30-minute slots)

## Status
Accepted

## Context
The system requires slot-based availability, overlap detection and occupancy analytics. Reservations and reports are accepted only on half-hour boundaries; operating hours may span midnight; a single timezone (`ZoneId.systemDefault()`) is used.

## Decision
Represent time as fixed-length slots (default 30 minutes) aligned to half-hour boundaries. Convert each reservation into the set of covered slots (closed-open intervals per slot) and evaluate capacity per slot. Generate slots in the restaurant's local time, persist and query timestamps in UTC.

## Rationale
- Simplifies overlap detection and correctness: capacity is checked per discrete slot rather than continuous times.
- Enables efficient aggregation for occupancy reports (slot-aligned buckets).
- Matches assignment constraints (half-hour increments) and keeps implementation simple.
- Handles overnight operating hours by generating slots that span midnight.

## Consequences
- Positive:
    - Fast, simple queries and aggregation using slot keys or range queries.
    - Deterministic capacity checks: any slot violation rejects the reservation.
- Negative:
    - Reduced temporal granularity (fixed 30-minute resolution).
    - Edge cases require careful handling: reservations must align to slot boundaries; spanning-midnight slots must be generated correctly.
    - Slots are computed in local time then mapped to UTC for storage/querying (watch for DST if multi-timezone support added later).

## Alternatives (rejected)
- Continuous interval arithmetic (per-minute checks): more precise but more complex and harder to aggregate.
- Variable-length slots: increases complexity for aggregation and fairness across bookings.
- Event-based or pre-aggregated counters (Redis): adds infra beyond assignment scope.

## Implementation notes
- Use closed-open slot intervals \[slotStart, slotEnd) to avoid double-counting boundaries.
- Reject requests not aligned to half-hour increments.
- Map local-time slots to UTC for persistence and overlap queries.