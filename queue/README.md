# AngusInfra — Queue Module

## Overview

The `queue` module provides a **database-backed persistent message queue** for AngusInfra. It
delivers SQS-style lease-based consumption semantics on top of a relational database (JPA), with no
dependency on external messaging middleware such as Kafka or RabbitMQ.

### Core Capabilities

| Feature                     | Description                                                                           |
|-----------------------------|---------------------------------------------------------------------------------------|
| **Multi-topic**             | Independent topics, each with its own message set                                     |
| **Partitioning**            | Configurable partitions per topic (default 8); deterministic routing by partition key |
| **Priority**                | Integer priority field; higher-priority messages are leased first                     |
| **Delayed messages**        | `visibleAt` field enables scheduled/deferred delivery                                 |
| **Lease-based consumption** | At-least-once semantics; lease expires if not acknowledged                            |
| **ACK / NACK**              | ACK marks message DONE; NACK re-enqueues with configurable backoff                    |
| **Dead Letter Queue (DLQ)** | Messages exceeding `maxAttempts` move to DLQ; supports replay and purge               |
| **Soft-delete DLQ**         | Optional mode: mark DLQ records deleted, purge after retention period                 |
| **Idempotency key**         | Optional deduplication key per message                                                |
| **Audit logging**           | Admin operations recorded via pluggable `AuditLogger`                                 |

---

## Architecture

### Module Layout

```
queue/
├── core/          # Domain logic; no Spring Boot dependency
│   └── cloud/xcan/angus/queue/core/
│       ├── service/
│       │   ├── QueueService.java              # Main queue API: send/lease/ack/nack
│       │   ├── DefaultQueueService.java
│       │   ├── QueueAdminService.java          # Admin API: stats/reclaim/purge/replay
│       │   ├── DefaultQueueAdminService.java
│       │   ├── AuditLogger.java                # Audit logging interface
│       │   └── Slf4jAuditLogger.java           # Default SLF4J implementation
│       ├── spi/
│       │   ├── RepositoryAdapter.java          # Persistence SPI (key extension point)
│       │   └── SoftDeleteDlqSupport.java       # Optional soft-delete DLQ capability
│       ├── entity/
│       │   ├── MessageEntity.java              # JPA entity → table: angus_mq_message
│       │   ├── DeadLetterEntity.java           # JPA entity → table: angus_mq_dead_letter
│       │   └── MessageStatus.java              # Enum: READY / LEASED / DONE
│       ├── model/                              # Request/response DTOs
│       ├── scheduler/
│       │   ├── LeaseReaper.java                # Reclaim expired leases (core logic)
│       │   └── DeadLetterMover.java            # Move over-limit messages to DLQ (core logic)
│       └── util/
│           └── Partitioner.java                # (topic, key) → partition hash
└── starter/       # Spring Boot auto-configuration
    └── cloud/xcan/angus/queue/starter/
        ├── QueueAutoConfiguration.java          # Auto-configuration entry point
        ├── adapter/JpaRepositoryAdapter.java    # JPA implementation of RepositoryAdapter
        ├── autoconfigure/QueueProperties.java   # Configuration properties
        ├── repository/
        │   ├── MessageRepository.java           # Spring Data JPA + native SQL
        │   └── DeadLetterRepository.java
        ├── scheduler/
        │   ├── LeaseReaperScheduler.java        # @Scheduled wrapper for LeaseReaper
        │   ├── DeadLetterMoverScheduler.java    # @Scheduled wrapper for DeadLetterMover
        │   └── DlqSoftDeletePurgerScheduler.java# Purges soft-deleted DLQ records
        └── web/
            ├── QueueController.java             # REST: /api/v1/queue (send/poll/ack/nack)
            └── AdminController.java             # REST: /api/v1/queue/admin
```

### Message Lifecycle

```
              send()
                │
                ▼
          ┌──────────┐
          │  READY   │◄──────────────────────────┐
          │ status=0 │                            │
          └────┬─────┘                            │ nack() with backoff
               │                                  │
            lease()                               │
               │                                  │
               ▼                                  │
          ┌──────────┐                            │
          │  LEASED  ├────────────────────────────┘
          │ status=1 │
          │          │── lease timeout ──► LeaseReaper reclaims ──► READY
          └────┬─────┘
               │
            ack()
               │
               ▼
          ┌──────────┐
          │   DONE   │──── purgeDone() cleanup
          │ status=2 │
          └──────────┘

  attempts >= maxAttempts
               │
               ▼
       ┌───────────────┐
       │  Dead Letter  │──── replay() ──► READY
       │  angus_mq_dead_letter│─── purge()  ──► deleted (hard or soft)
       └───────────────┘
```

### Partitioning

Messages are deterministically routed to a partition by hashing `topic + "#" + partitionKey`:

```java
public static int partition(String topic, String key, int numPartitions) {
    String base = topic + "#" + (key == null ? "" : key);
    int h = base.hashCode();
    return Math.floorMod(h, Math.max(1, numPartitions));
}
```

Messages sharing the same `partitionKey` always land on the same partition, enabling
**ordered processing within a partition**. Consumers can subscribe to a subset of partitions for
load-balanced parallel consumption.

---

## Key Interfaces

### `QueueService` — Queue Operations

```java
public interface QueueService {
    Long send(String topic, String partitionKey, String payload,
              String headers, int priority, Instant visibleAt,
              String idempotencyKey, int maxAttempts, int numPartitions);

    default Long send(SendMessage req) { ... }          // convenient DTO overload

    int lease(String topic, Collection<Integer> partitions,
              String owner, int leaseSec, int limit);   // READY → LEASED

    List<MessageData> listLeasedByOwner(String owner, int limit);

    int ack(Collection<Long> ids);                      // LEASED → DONE

    int nack(Collection<Long> ids, int backoffSec);     // LEASED → READY (+attempts, +backoff)

    int moveExceededAttemptsToDeadLetter(int limit);
}
```

### `QueueAdminService` — Admin Operations

```java
public interface QueueAdminService {
    Map<String, Object> topicStats(String topic);       // status counts, DLQ count, per-partition ready
    int reclaimExpired(int limit);                      // reclaim timed-out leases
    int purgeDone(String topic, Instant before);        // delete old DONE messages
    int purgeDeadLetters(String topic);                 // delete/soft-delete DLQ
    int replayFromDeadLetter(String topic, int limit);  // DLQ → READY
}
```

### `RepositoryAdapter` — Persistence SPI

The core module has zero dependency on Spring or JPA. All persistence is accessed through this
interface, making it easy to swap in a custom storage backend:

```java
public interface RepositoryAdapter {
    Long saveMessage(SendMessage msg);
    int leaseBatch(String topic, Collection<Integer> partitions,
                   String owner, int leaseSec, int limit);
    List<MessageData> findLeasedByOwner(String owner, int limit);
    int ackBatch(Collection<Long> ids);
    int nackBatch(Collection<Long> ids, int backoffSec);
    int moveExceededToDeadLetter(int limit);
    int reclaimExpiredLeases(int limit);
    // ... stats, purge, DLQ operations
}
```

---

## Database Schema

### `angus_mq_message` — Main message table

| Column            | Type                  | Description                               |
|-------------------|-----------------------|-------------------------------------------|
| `id`              | BIGINT AUTO_INCREMENT | Primary key                               |
| `topic`           | VARCHAR(128)          | Message topic                             |
| `partition_id`    | INT                   | Partition index                           |
| `priority`        | INT                   | Higher = earlier lease (default 0)        |
| `payload`         | JSON                  | Message body                              |
| `headers`         | JSON                  | Optional metadata                         |
| `status`          | TINYINT               | 0=READY, 1=LEASED, 2=DONE                 |
| `visible_at`      | TIMESTAMP             | Earliest delivery time (delayed messages) |
| `lease_until`     | TIMESTAMP             | Lease expiry (set by consumer)            |
| `lease_owner`     | VARCHAR(128)          | Consumer identity holding the lease       |
| `attempts`        | INT                   | Delivery attempts so far                  |
| `max_attempts`    | INT                   | Max attempts before DLQ (default 16)      |
| `idempotency_key` | VARCHAR(256)          | Optional deduplication key                |
| `created_at`      | TIMESTAMP             | Creation time                             |
| `updated_at`      | TIMESTAMP             | Last update time                          |
| `version`         | BIGINT                | Optimistic-lock version                   |

### `angus_mq_dead_letter` — Dead letter queue table

| Column         | Type                  | Description                           |
|----------------|-----------------------|---------------------------------------|
| `id`           | BIGINT AUTO_INCREMENT | Primary key                           |
| `topic`        | VARCHAR(128)          | Message topic                         |
| `partition_id` | INT                   | Partition index                       |
| `payload`      | JSON                  | Message body                          |
| `headers`      | JSON                  | Optional metadata                     |
| `attempts`     | INT                   | Attempt count when moved to DLQ       |
| `reason`       | VARCHAR(256)          | Reason for DLQ move                   |
| `created_at`   | TIMESTAMP             | DLQ entry creation time               |
| `deleted_at`   | TIMESTAMP             | Soft-delete timestamp (NULL = active) |

---

## Quick Start

### 1. Add dependencies

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.queue-starter</artifactId>
  <version>3.0.0</version>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Database driver, e.g. MySQL -->
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
</dependency>
```

### 2. Configure datasource

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC
    username: root
    password: yourpassword
  jpa:
    hibernate:
      ddl-auto: update      # dev only; use validate + manual DDL in production

angus:
  queue:
    partitions: 8
    lease-seconds: 30
```

### 3. Create tables (production — recommended over ddl-auto)

- MySQL: `queue/core/src/main/resources/schema/mysql/queue-schema.sql`
- PostgreSQL: `queue/core/src/main/resources/schema/postgres/queue-schema.sql`

### 4. Produce and consume messages

**Producer:**

```java
@Service
public class OrderEventProducer {

    private final QueueService queueService;

    public void sendOrderPaid(Long orderId) {
        queueService.send(SendMessage.builder()
            .topic("order-events")
            .partitionKey("order-" + orderId)   // same order → same partition → ordered
            .payload("{\"orderId\":" + orderId + ",\"status\":\"PAID\"}")
            .headers("{\"source\":\"payment-service\"}")
            .priority(10)
            .maxAttempts(5)
            .idempotencyKey("order-" + orderId + "-PAID")
            .build());
    }

    /** Delayed delivery: visible 5 minutes from now */
    public void sendReminder(Long orderId) {
        queueService.send(SendMessage.builder()
            .topic("order-reminders")
            .partitionKey("order-" + orderId)
            .payload("{\"orderId\":" + orderId + "}")
            .visibleAt(Instant.now().plusSeconds(300))
            .build());
    }
}
```

**Consumer:**

```java
@Component
public class OrderEventConsumer {

    private final QueueService queueService;
    private final String OWNER = "node-" + InetAddress.getLocalHost().getHostName();

    @Scheduled(fixedDelay = 2000)
    public void poll() {
        // 1. Lease READY messages
        queueService.lease("order-events",
            IntStream.range(0, 8).boxed().toList(),   // all partitions
            OWNER, 60, 50);

        // 2. Fetch leased messages for this owner
        List<MessageData> messages = queueService.listLeasedByOwner(OWNER, 50);

        List<Long> ackIds  = new ArrayList<>();
        List<Long> nackIds = new ArrayList<>();

        for (MessageData msg : messages) {
            try {
                process(msg);
                ackIds.add(msg.getId());
            } catch (Exception e) {
                log.warn("Processing failed for msg {}, nacking", msg.getId(), e);
                nackIds.add(msg.getId());
            }
        }

        // 3. Bulk ACK / NACK
        if (!ackIds.isEmpty())  queueService.ack(ackIds);
        if (!nackIds.isEmpty()) queueService.nack(nackIds, 30); // retry after 30s
    }
}
```

---

## Configuration Reference

```yaml
angus:
  queue:
    partitions: 8                       # Partitions per topic used when sending. Default: 8
    poll-batch: 100                     # Max messages per poll. Default: 100
    ack-batch: 200                      # Max IDs per bulk ACK. Default: 200
    lease-seconds: 30                   # Default lease duration (seconds). Default: 30
    reclaim-batch: 500                  # LeaseReaper batch size. Default: 500
    dead-letter-move-batch: 200         # DeadLetterMover batch size. Default: 200

    reclaim-interval-ms: 3000           # LeaseReaperScheduler interval (ms). Default: 3000
    dead-letter-move-interval-ms: 5000  # DeadLetterMoverScheduler interval (ms). Default: 5000

    scheduling:
      enabled: true                     # Enable background schedulers. Default: true
      pool-size: 4                      # Scheduler thread pool size. Default: 4
      thread-name-prefix: "queue-scheduler-"

    admin:
      soft-delete-dlq: false            # Use soft-delete for DLQ purge. Default: false
      retention-days: 7                 # Soft-deleted DLQ retention (days). Default: 7
      purge-interval-ms: 600000         # Soft-delete purge interval (ms). Default: 600000
```

---

## REST API

### Queue API — `/api/v1/queue`

| Method | Path                 | Description                                               |
|--------|----------------------|-----------------------------------------------------------|
| `POST` | `/api/v1/queue/send` | Send a message to a topic                                 |
| `POST` | `/api/v1/queue/poll` | Lease READY messages (READY → LEASED), return leased list |
| `POST` | `/api/v1/queue/ack`  | Acknowledge messages (LEASED → DONE)                      |
| `POST` | `/api/v1/queue/nack` | Nack messages with backoff (LEASED → READY, attempts+1)   |

**Send request body:**

```json
{
  "topic": "order-events",
  "partitionKey": "order-123",
  "payload": "{\"orderId\":123,\"status\":\"PAID\"}",
  "priority": 10,
  "visibleAt": "2026-04-01T10:00:00Z",
  "idempotencyKey": "order-123-PAID",
  "maxAttempts": 5
}
```

**Poll request body:**

```json
{ "topic": "order-events", "owner": "worker-1", "partitions": [0,1,2,3], "leaseSeconds": 60, "limit": 50 }
```

**ACK / NACK request body:**

```json
{ "ids": [101, 102, 103] }
{ "ids": [104, 105], "backoffSeconds": 30 }
```

### Admin API — `/api/v1/queue/admin`

| Method   | Path                                                    | Description                                                      |
|----------|---------------------------------------------------------|------------------------------------------------------------------|
| `GET`    | `/api/v1/queue/admin/stats?topic=<t>`                   | Topic stats: status distribution, DLQ count, ready-per-partition |
| `POST`   | `/api/v1/queue/admin/reclaim?limit=500`                 | Manually trigger lease reclaim                                   |
| `DELETE` | `/api/v1/queue/admin/purge/done?topic=<t>&before=<iso>` | Purge DONE messages before timestamp                             |
| `DELETE` | `/api/v1/queue/admin/purge/dlq?topic=<t>`               | Purge DLQ (hard or soft per config)                              |
| `POST`   | `/api/v1/queue/admin/dlq/replay?topic=<t>&limit=100`    | Replay DLQ messages back to READY                                |

---

## Advanced Integration

### Custom `RepositoryAdapter`

To replace the JPA backend (e.g. add PostgreSQL-specific optimizations or use a different store),
implement `RepositoryAdapter` and register it as a Spring bean. The
`@ConditionalOnMissingBean(RepositoryAdapter.class)` guard ensures your bean takes precedence:

```java
@Component
public class MyRepositoryAdapter implements RepositoryAdapter {
    @Override
    public Long saveMessage(SendMessage msg) { ... }
    // implement remaining methods
}
```

### Custom `AuditLogger`

Register a bean implementing `AuditLogger` to replace the default `Slf4jAuditLogger`:

```java
@Component
public class DatabaseAuditLogger implements AuditLogger {
    @Override
    public void adminAction(String action, String topic, int affected, String detail) {
        // persist to audit table
    }
}
```

### Partition-based Load Balancing (multi-node)

Assign disjoint partition sets to each node for parallel, ordered consumption:

```java
// Node 0 consumes partitions 0-3
queueService.lease("order-events", List.of(0, 1, 2, 3), "node-0", 60, 100);

// Node 1 consumes partitions 4-7
queueService.lease("order-events", List.of(4, 5, 6, 7), "node-1", 60, 100);
```

### Soft-delete DLQ

Enable soft-delete mode to retain DLQ entries for post-mortem inspection before physical removal:

```yaml
angus:
  queue:
    admin:
      soft-delete-dlq: true
      retention-days: 14
      purge-interval-ms: 3600000
```

In this mode, `purgeDeadLetters()` sets `deleted_at = NOW()` instead of issuing a `DELETE`.
`DlqSoftDeletePurgerScheduler` runs periodically to hard-delete records older than `retention-days`.

---

## Background Schedulers

| Scheduler                      | Default Interval | Config key                                 | Function                                 |
|--------------------------------|------------------|--------------------------------------------|------------------------------------------|
| `LeaseReaperScheduler`         | 3 s              | `angus.queue.reclaim-interval-ms`          | Reset timed-out LEASED → READY           |
| `DeadLetterMoverScheduler`     | 5 s              | `angus.queue.dead-letter-move-interval-ms` | Move over-limit messages to DLQ          |
| `DlqSoftDeletePurgerScheduler` | 10 min           | `angus.queue.admin.purge-interval-ms`      | Hard-delete old soft-deleted DLQ records |

Disable all schedulers with `angus.queue.scheduling.enabled=false` if you prefer to manage these
tasks externally (e.g. via Quartz or a dedicated maintenance service).

---

## FAQ

**Q: Is message delivery guaranteed exactly once?**

A: No — the module provides **at-least-once** semantics. If a consumer crashes before ACKing,
`LeaseReaper` reclaims the message and it will be delivered again. Design your consumers to be
idempotent, or use the `idempotencyKey` field to deduplicate at the sender.

**Q: How do I replay DLQ messages?**

A: Call `POST /api/v1/queue/admin/dlq/replay?topic=<t>&limit=100` or invoke
`QueueAdminService.replayFromDeadLetter(topic, limit)` directly. Replayed messages are re-inserted
into `angus_mq_message` as READY; the DLQ records are deleted.

**Q: Can I disable the background schedulers?**

A: Yes. Set `angus.queue.scheduling.enabled=false`. You then control when lease reclaim and DLQ
moves happen — useful in test environments or when using an external scheduler.

**Q: What happens when `maxAttempts` is reached?**

A: `DeadLetterMoverScheduler` detects messages where `attempts >= max_attempts` and moves them to
`angus_mq_dead_letter`. They no longer appear in `poll` results and must be handled via the admin
API
(replay or purge).

---

## Building and Testing

```bash
# Build the complete queue module
mvn -pl queue -am clean install

# Run core unit tests only
mvn -pl queue/core test

# Run starter unit tests only
mvn -pl queue/starter test
```

## Contributing

Contributions welcome. Please:

- Add unit tests for any behavior changes
- Keep the `core` module free of Spring Boot dependencies
- Keep public APIs backward compatible where possible
