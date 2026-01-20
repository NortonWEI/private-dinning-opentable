# ADR‑UTC‑Time‑Handling: Persist instants in UTC

## Status

Accepted

## Context

The system evaluates business rules (operating hours, slot generation, capacity checks) in the restaurant's local time (using `ZoneId.systemDefault()`). The persistence layer (MongoDB) must store timestamps so that range queries, overlap detection and aggregation are correct, efficient and unambiguous across DST and operational boundaries.

## Decision

Persist all date/time instants in UTC in the database. Continue to evaluate business rules and generate slots in local date/time. Convert local slot boundaries to UTC when storing and when querying reservations and reports. Persist the restaurant's time‑zone identifier when multi‑zone support is added; for now rely on `ZoneId.systemDefault()` for local evaluation.

## Rationale

- Single canonical timeline: UTC removes ambiguity about offsets and DST, so comparisons and range queries are deterministic.
- Efficient indexing and range queries: databases and indexes operate reliably on a single epoch-based timeline (e.g. `Instant`), enabling fast overlap queries and aggregations.
- Interoperability: UTC is a standard interchange format for logs, metrics and external integrations.
- Simplicity for concurrency and distributed systems: using UTC simplifies reasoning about locking, optimistic checks and replication across hosts in different time zones.

## Consequences

Positive:

- Reliable and fast DB queries for reservations and reporting.
- Predictable behaviour across DST transitions and host time‑zones.
- Easy to expose UTC timestamps in APIs for client conversions.

Negative:

- Business logic must explicitly convert between local LocalDateTime and UTC Instant when generating slots and validating requests.
- UTC persistence loses local offset/context. Store the restaurant time‑zone (when needed) to reconstitute local presentation.
- Care needed when validating requests that arrive in local time: validation must use the restaurant local time before converting to UTC.