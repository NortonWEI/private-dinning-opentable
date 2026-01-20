# ADR‑Occupancy‑Reporting: Slot‑aligned occupancy report

## Status

Accepted

## Context

The system must produce occupancy time‑series for a restaurant (or a specific space) over a requested range. Front‑end consumers need data that is trivial to plot: consistent time buckets, numeric metrics per bucket, stable ordering and clear labels. The system uses fixed 30‑minute slots, a single timezone (ZoneId.systemDefault()), and persists timestamps in UTC.

## Decision

Return slot‑aligned occupancy reports at the system granularity (default 30 minutes). Each ordered time‑series point includes:

- slotStart (UTC ISO timestamp)
- slotEnd (UTC ISO timestamp)
- capacity (integer)
- occupancy (integer)
- occupancyRate (decimal 0..1)

Support:

- space‑level reports when spaceId is provided
- restaurant‑level reports (aggregate of all spaces) otherwise

Reject requests not aligned to slot boundaries or that exceed allowed duration limits.

## Rationale

- Slot‑aligned buckets map directly to chart x‑axis buckets and make front‑end visualisation trivial.
- Fixed fields per point avoid client‑side data massaging.
- Returning UTC ISO timestamps removes ambiguity and simplifies client time‑axis handling.
- Aggregating restaurant capacity as the sum of spaces' max capacities matches capacity semantics used by reservation logic.

## Consequences

Positive:

- Front‑end visualisation is straightforward: stable time axis, ready‑to‑plot numeric values, and predictable empty buckets.
- Back‑end aggregation is simple: bucket reservations into slots and sum party sizes per slot.

Negative:

- Fixed 30‑minute granularity reduces temporal resolution.
- Clients must convert UTC timestamps to local display timezone if needed.
- Requests must be aligned to half‑hour boundaries; misaligned requests are rejected.