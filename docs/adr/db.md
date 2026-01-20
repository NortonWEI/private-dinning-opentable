# ADR-DB: Embedded MongoDB

## Status
Accepted

## Context
The assignment provides an embedded MongoDB. The system needs time-range queries, overlap detection, and slot-based aggregation for reservations and reporting.

## Decision
Use the provided embedded MongoDB and store all timestamps in UTC.

## Rationale
- Matches the assignment constraints; no extra infra.
- MongoDB is well-suited for indexed time-range queries and aggregation pipelines needed for slot grouping, overlap detection, and occupancy metrics.

## Alternatives
- SQL: not chosen due to assignment constraints and heavier relational needs.
- In-memory/file storage: rejected for persistence and concurrency realism.

## Consequences
- Positive: no extra infra, efficient time queries and aggregations.
- Negative: capacity enforcement handled in application code; fewer relational guarantees than SQL.