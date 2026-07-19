# Database Message Queue (angus-infra-queue)

[English](README.md) | [中文](README_zh.md)

A relational-DB (JPA) backed message queue with SQS-like **lease consumption**. No Kafka/RabbitMQ
required. Delivery is **at-least-once** — consumers must be idempotent.

> **Not for:** ultra-high throughput streaming, or exactly-once guarantees.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Data Model](#3-data-model)
4. [Configuration](#4-configuration)
5. [Integration Guide](#5-integration-guide)
6. [API Reference](#6-api-reference)
7. [Lease / DLQ / Schedulers](#7-lease--dlq--schedulers)
8. [Management REST](#8-management-rest)
9. [Best Practices](#9-best-practices)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Overview

| Piece | Responsibility |
|-------|----------------|
| `QueueService` | Send / lease / ack / nack |
| `QueueAdminService` | Stats, reclaim, purge, DLQ replay |
| `RepositoryAdapter` | Persistence SPI (default JPA) |
| `LeaseReaperScheduler` | Reclaim expired leases |
| `DeadLetterMoverScheduler` | Move exceeded attempts to DLQ |
| `queue-web` | Optional REST (not auto-wired by starter) |

**Design principles:** DB-backed; Spring-decoupled core via SPI; at-least-once leases;
partition-ordered by `(topic, partitionKey)`; optional management surface.

---

## 2. Architecture

```
send → READY → lease → LEASED → ack(DONE) | nack(READY+backoff)
LeaseReaper: expired LEASED → READY (attempts unchanged)
DeadLetterMover: attempts >= max → DLQ + delete
```

Status ordinals: `READY=0`, `LEASED=1`, `DONE=2`.

---

## 3. Data Model

Tables: `angus_mq_message`, `angus_mq_dead_letter`  
Scripts: `classpath:schema/mysql/queue-schema.sql` (Postgres sibling).

`idempotency_key` is stored only — **no unique index / no forced dedupe**.

---

## 4. Configuration

Prefix: `angus.queue`

| Property | Default | Notes |
|----------|---------|-------|
| `partitions` | `8` | Used by REST send |
| `poll-batch` | `100` | Default poll size |
| `ack-batch` | `200` | Bound but unused |
| `lease-seconds` | `30` | Default lease |
| `reclaim-batch` | `500` | Reaper batch |
| `dead-letter-move-batch` | `200` | DLQ mover batch |
| `scheduling.pool-size` | `4` | Scheduler pool |
| `admin.retention-days` | `7` | Soft-delete retention |
| `admin.purge-interval-ms` | `600000` | Soft-delete purger |

Also used via placeholders / conditions (not all are Java fields):

| Property | Default | Notes |
|----------|---------|-------|
| `scheduling.enabled` | `true` | Register reaper/mover |
| `admin.soft-delete-dlq` | `false` | Soft delete + purger |
| `reclaim-interval-ms` | `3000` | Reaper delay |
| `dead-letter-move-interval-ms` | `5000` | Mover delay |

---

## 5. Integration Guide

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.queue-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

Optional: `xcan-angusinfra.queue-web` + scan `cloud.xcan.angus.queue.web` + `PPS` security.

**Critical:** programmatic `send(SendMessage)` defaults `numPartitions=1`, not
`angus.queue.partitions`. Always set partitions explicitly for producers.

```java
queueService.send(SendMessage.builder()
    .topic("order-events")
    .partitionKey("order-" + orderId)
    .payload("{\"orderId\":1}")
    .numPartitions(8)
    .maxAttempts(5)
    .build());
```

---

## 6. API Reference

`QueueService`: `send`, `lease`, `listLeasedByOwner`, `ack`, `nack`,
`moveExceededAttemptsToDeadLetter`.

`QueueAdminService`: `listTopics`, `topicStats`, `reclaimExpired`, `purgeDone`,
`purgeDeadLetters`, `replayFromDeadLetter`.

---

## 7. Lease / DLQ / Schedulers

- **nack** increments `attempts`; **reaper** does not.
- Replay creates a new READY message (`attempts=0`, `maxAttempts=16`) without the original
  idempotency key.
- `scheduling.enabled=false` skips reaper/mover beans; purger only follows `soft-delete-dlq`.

---

## 8. Management REST

`/api/v1/queue` and `/api/v1/queue/admin`, secured by sys-admin `PPS` checks.

---

## 9. Best Practices

1. Make consumers idempotent.
2. Align producer `numPartitions` with consumer partition assignment.
3. Assign disjoint partitions across consumer nodes.
4. Do not rely on `idempotency_key` for uniqueness unless you add your own constraint.
5. Size `lease-seconds` above worst-case processing time.
6. Periodically purge DONE / DLQ in production.

---

## 10. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Duplicates | Missed ack / lease expiry | Idempotency + larger lease |
| Message invisible | `visible_at` / wrong partition | Inspect row |
| Fast DLQ | Small `maxAttempts` / frequent nack | Tune + fix consumer |
| Schedulers idle | `scheduling.enabled=false` | Enable or reclaim manually |
| All partition 0 | Default `numPartitions=1` | Set explicitly |

---

## Related classes

| Class | Module |
|-------|--------|
| `cloud.xcan.angus.queue.service.QueueService` | `queue-core` |
| `cloud.xcan.angus.queue.autoconfigure.QueueAutoConfiguration` | `queue-starter` |
| `cloud.xcan.angus.queue.web.QueueController` | `queue-web` |
