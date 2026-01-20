# ADR-Concurrency: Optimistic Locking with In-JVM Serialization

## Status
Accepted

## Context
Prevent overbooking for slot-based capacity under concurrent reservation requests.

## Decision
Use an in‑JVM `ReentrantLock` to serialize reservation creation per restaurant/space, plus JPA/MongoDB optimistic locking (`@Version`) as a safety net.

## Rationale
- `ReentrantLock` ensures capacity checks run against a consistent, up-to-date view.
- `@Version` detects stale writes and prevents lost updates at persistence time.
- Avoid pessimistic DB locks to keep implementation simple and non-blocking for a single-node embedded setup.

## Alternatives (rejected)
- Pessimistic DB locking: blocking, complex, unnecessary for this assignment.
- Optimistic-only: race windows may cause repeated retries and complex logic.
- Distributed locking (Redis/ZK): adds infra beyond scope.

## Consequences
- Positives: correctness for capacity checks, simple model, avoids DB-level blocking.
- Negatives: throughput is limited under high contention.