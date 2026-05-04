ID Generator — idgen Module
==========================

> **Note**: AngusInfra idgen is a distributed ID generation component based on Baidu's
> [UidGenerator](https://github.com/baidu/uid-generator), extended with a business-readable
> ID generator (BidGenerator).
> Key changes from the original: ① Replaced MyBatis with JPA; ② Replaced Spring XML with
> Spring Boot auto-configuration; ③ Removed MySQL-only dependency (PostgreSQL / Oracle /
> SQL Server now supported); ④ Added BidGenerator for human-readable business codes.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Core Components](#3-core-components)
4. [Database Schema](#4-database-schema)
5. [Configuration Reference](#5-configuration-reference)
6. [Integration Guide](#6-integration-guide)
7. [Performance](#7-performance)
8. [Important Notes](#8-important-notes)

---

## 1. Overview

The idgen module provides two categories of ID generators:

| Generator        | Output Type                            | Use Cases                                                                  |
|------------------|----------------------------------------|----------------------------------------------------------------------------|
| **UidGenerator** | 64-bit `long`, Snowflake-variant       | Database PKs, distributed trace IDs, high-throughput internal IDs          |
| **BidGenerator** | Readable `String`, configurable format | Order numbers, contract codes, ticket IDs, any human-facing business codes |

Both are integrated as a Spring Boot Starter and activated by `angus.idgen.enabled=true`.

---

## 2. Architecture

### 2.1 UidGenerator (Snowflake Variant)

```
 63      62...(62-timeBits+1)   ...(workerBits)   ...(seqBits)   0
  ┌─────┬──────────────────────┬─────────────────┬───────────────┐
  │sign │    deltaSeconds      │   workerId      │   sequence    │
  │ 1b  │      28b(default)    │    22b(default) │   13b(default)│
  └─────┴──────────────────────┴─────────────────┴───────────────┘
```

- **sign (1 bit)**: Always 0, ensuring the generated ID is a positive number.
- **deltaSeconds (28 bit default)**: Seconds since the epoch (`2016-05-20`), covering ~8.7 years.
- **workerId (22 bit default)**: Instance ID assigned from the auto-increment `id` column in
  the `angus_instance` table, supporting up to ~4M instances.
- **sequence (13 bit default)**: Per-second sequence counter, up to 8192 IDs per second per
  instance.

> All bit widths are configurable; the sum of the three segments must equal 63.

**DefaultUidGenerator**: Synchronous generation under `synchronized nextId()`. No extra memory
overhead. Suitable for low-concurrency or strict ordering requirements.

**CachedUidGenerator**: Extends `DefaultUidGenerator` with a RingBuffer pre-generation mechanism:

- Pre-fills the RingBuffer at startup (default capacity: `(maxSequence+1) << boostPower` ≈ 32,768
  slots).
- Consumer threads take UIDs from the buffer head; a background thread asynchronously refills when
  the remaining slots drop below `paddingFactor%` (default 50%).
- A scheduled refill (default every 5 minutes) prevents exhaustion during low-traffic periods.
- Falls back to synchronous generation (`super.nextId()`) if the RingBuffer is exhausted — no
  exception thrown.
- Uses `PaddedAtomicLong` (Cache Line alignment) to eliminate false sharing. Achieves up to **6
  million QPS** on a single instance.

### 2.2 BidGenerator (Business ID)

```
┌─────────────────────┐     ┌──────────────────────────┐
│  Caller              │────▶│  DefaultBidGenerator     │
│  getId(bizKey)       │     │  ConcurrentHashMap(cache)│
└─────────────────────┘     └──────────┬───────────────┘
                                         │ cache miss / segment exhausted
                    ┌────────────────────┴────────────────────┐
                    │                                          │
              ┌─────▼──────┐                        ┌─────────▼──────┐
              │  DB Mode    │                        │  Redis Mode     │
              │ Fetch range │                        │ INCRBY atomic   │
              │ from DB,    │                        │ no local segment│
              │ cache locally│                       │ needed          │
              └─────────────┘                        └─────────────────┘
```

- **DB mode**: Reads `maxId` + `step` from `id_config`, maintains an in-memory `AtomicLong` counter,
  re-fetches a new segment when exhausted. Double-checked locking (DCL) ensures concurrent safety.
- **Redis mode**: Uses `INCRBY` for atomic increments. No local counter; suitable for multi-instance
  deployments with strict consistency requirements.
- **PLATFORM scope**: All tenants share one number space (`tenantId = -1`).
- **TENANT scope**: Each tenant has an independent number space. On the first call for a new tenant,
  the generator automatically clones the template row (`tenantId=-1`) into a new tenant-specific
  row.

---

## 3. Core Components

### 3.1 UidGenerator Family

| Class / Interface              | Description                                                                                                                               |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `UidGenerator`                 | Interface: `getUID() → long`, `parseUID(long) → String`                                                                                   |
| `BitsAllocator`                | Bit manipulation utility encapsulating shifts and masks for each segment                                                                  |
| `DefaultUidGenerator`          | Synchronous Snowflake implementation; handles clock rollback and sequence overflow                                                        |
| `CachedUidGenerator`           | Extends `DefaultUidGenerator`; adds RingBuffer + `BufferPaddingExecutor`                                                                  |
| `RingBuffer`                   | Array-based lock-free ring buffer; `tail`/`cursor` are `PaddedAtomicLong`                                                                 |
| `BufferPaddingExecutor`        | Manages async RingBuffer refill via thread pool and scheduled tasks                                                                       |
| `InstanceIdAssigner`           | SPI: `assignInstanceIdByEnv()` and `assignInstanceIdByParam()`                                                                            |
| `DisposableInstanceIdAssigner` | Default impl: reads `HOST`/`HTTP_PORT`/`RUNTIME_ENV` env vars, inserts to `angus_instance` table, returns auto-increment `id` as workerId |

### 3.2 BidGenerator Family

| Class / Interface         | Description                                                                  |
|---------------------------|------------------------------------------------------------------------------|
| `BidGenerator`            | Interface: `getId` / `getIds` with optional `tenantId` overloads             |
| `AbstractBidGenerator`    | Abstract base: assembles formatted output (PREFIX + DATE + SEQ combinations) |
| `DefaultBidGenerator`     | Default impl: `ConcurrentHashMap` in-memory cache + DCL initialization       |
| `ConfigIdAssigner`        | SPI: reads/writes `angus_id_config` table and allocates segments             |
| `DistributedIncrAssigner` | SPI: wraps Redis `INCRBY` atomic increment                                   |

### 3.3 Enumerations

#### Format (Code Format)

| Value             | Output Example        | Description              |
|-------------------|-----------------------|--------------------------|
| `SEQ`             | `00000001`            | Sequence number only     |
| `PREFIX_SEQ`      | `ORD00000001`         | Prefix + sequence        |
| `DATE_SEQ`        | `2024090100000001`    | Date + sequence          |
| `PREFIX_DATE_SEQ` | `ORD2024090100000001` | Prefix + date + sequence |

#### Mode (Generation Mode)

| Value   | Description                                                                               |
|---------|-------------------------------------------------------------------------------------------|
| `DB`    | Segment fetched from `id_config.max_id` + `step`; local `AtomicLong` consumes the segment |
| `REDIS` | Each call uses Redis `INCRBY` atomically; no local segment cache                          |

#### Scope (Uniqueness Scope)

| Value      | Behavior                                                                                       |
|------------|------------------------------------------------------------------------------------------------|
| `PLATFORM` | All callers share the `tenantId=-1` config row; platform-wide uniqueness                       |
| `TENANT`   | First call for a new tenant clones the template row (`tenantId=-1`) into a tenant-specific row |

#### DateFormat

| Value      | Example    |
|------------|------------|
| `YYYY`     | `2024`     |
| `YYYYMM`   | `202409`   |
| `YYYYMMDD` | `20240901` |

---

## 4. Database Schema

### 4.1 `angus_instance` Table (WorkerId Assignment)

```sql
CREATE TABLE `angus_instance` (
  `pk`            varchar(40)  NOT NULL COMMENT 'Primary key (UUID)',
  `id`            bigint(21)   NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment workerId for Snowflake',
  `host`          varchar(160) NOT NULL DEFAULT '' COMMENT 'Instance IP or hostname',
  `port`          varchar(40)  NOT NULL DEFAULT '' COMMENT 'Instance HTTP port',
  `instance_type` varchar(40)  NOT NULL DEFAULT '' COMMENT 'Instance type (from RUNTIME_ENV env var)',
  `create_date`   datetime     NOT NULL COMMENT 'Registration time',
  `modified_date` datetime     NOT NULL COMMENT 'Last update time',
  PRIMARY KEY (`pk`),
  UNIQUE KEY `uidx_host_port` (`host`, `port`) USING BTREE
) ENGINE=InnoDB COMMENT='UID generator instance registry';
```

**Field Notes**:

- `id`: Auto-increment long, used as the Snowflake `workerId`. Must not exceed `2^workerBits - 1`.
- `host` + `port`: Unique constraint ensures the same instance reuses its `workerId` on restart.
- Environment variable mapping: `HOST` → host, `HTTP_PORT` → port, `RUNTIME_ENV` → instance_type.

### 4.2 `angus_id_config` Table (Business ID Configuration)

```sql
CREATE TABLE `angus_id_config` (
  `pk`           varchar(40)  NOT NULL COMMENT 'Primary key (UUID)',
  `biz_key`      varchar(80)  NOT NULL COMMENT 'Business identifier, e.g. order, contract',
  `format`       varchar(16)  NOT NULL COMMENT 'Code format: SEQ/PREFIX_SEQ/DATE_SEQ/PREFIX_DATE_SEQ',
  `prefix`       varchar(4)   NOT NULL DEFAULT '' COMMENT 'Code prefix (1–4 characters)',
  `date_format`  varchar(8)   NOT NULL DEFAULT '' COMMENT 'Date format: YYYY/YYYYMM/YYYYMMDD',
  `seq_length`   int(11)      NOT NULL DEFAULT 8 COMMENT 'Sequence digits; <=0 for variable length',
  `mode`         varchar(8)   NOT NULL COMMENT 'Generation mode: DB/REDIS',
  `scope`        varchar(16)  NOT NULL COMMENT 'Uniqueness scope: PLATFORM/TENANT',
  `tenant_id`    bigint(20)   NOT NULL DEFAULT -1 COMMENT 'Tenant ID; -1 means platform-level',
  `max_id`       bigint(20)   NOT NULL DEFAULT 0 COMMENT 'DB mode: current maximum allocated value',
  `step`         bigint(20)   NOT NULL COMMENT 'Segment step size; recommended 1000–10000',
  `create_date`  datetime     NOT NULL COMMENT 'Creation time',
  `modified_date` datetime    NOT NULL COMMENT 'Last modification time',
  PRIMARY KEY (`pk`),
  UNIQUE INDEX `uidx_biz_key_tenant_id` (`biz_key`, `tenant_id`)
) ENGINE=InnoDB COMMENT='Business ID configuration table';
```

**Key Constraints**:

- `(biz_key, tenant_id)` unique index: Under TENANT scope, each tenant has its own row.
- `max_id`: DB mode records the upper bound of the currently allocated segment; updated
  via `UPDATE ... SET max_id = max_id + step` on each fetch.
- `step` recommended range: 1,000–10,000. Too small → frequent I/O; too large → wasted IDs on
  restart.

---

## 5. Configuration Reference

```yaml
angus:
  idgen:
    enabled: true                   # Master switch; activates all beans when true

    # ── UidGenerator Bit Allocation ──────────────────────────────────
    uid:
      timeBits: 28                  # Time segment bits (seconds); determines max lifetime
      workerBits: 22                # WorkerId bits; max instances = 2^22 ≈ 4M
      seqBits: 13                   # Per-second sequence bits; max QPS = 2^13 ≈ 8192/s
      epochStr: "2016-05-20"        # Reference epoch date; set to project launch date
      retriesNum: 3                 # Retry count on clock rollback

    # ── CachedUidGenerator RingBuffer Tuning ─────────────────────────
    cached:
      boostPower: 2                 # Buffer size multiplier: bufferSize = (maxSeq+1) << boostPower
      paddingFactor: 50             # Trigger async fill when remaining slots < this % (0–100)
      scheduleInterval: 300         # Scheduled refill interval (seconds); 0 disables scheduling
      rejectionPolicy: BLOCK        # Policy when RingBuffer is full: BLOCK / DISCARD

    # ── BidGenerator ─────────────────────────────────────────────────
    bid:
      initialMapCapacity: 512       # Initial capacity of the id_config in-memory cache map

# Required when using Redis mode for BidGenerator
xcan:
  redis:
    enabled: true
```

### Configuration Properties

| Property                  | Default    | Description                                                     |
|---------------------------|------------|-----------------------------------------------------------------|
| `uid.timeBits`            | 32         | Seconds precision; 32 bits ≈ 136 years from epoch               |
| `uid.workerBits`          | 22         | `timeBits + workerBits + seqBits` must equal 63                 |
| `uid.seqBits`             | 13         | Max IDs per second per instance = `2^seqBits`                   |
| `uid.epochStr`            | 2016-05-20 | Changing this requires resetting the `angus_instance` table     |
| `cached.boostPower`       | 2          | At `boostPower=2`: RingBuffer ≈ 32,768 slots ≈ 256 KB RAM       |
| `cached.paddingFactor`    | 50         | 50% means async fill starts when half the buffer is consumed    |
| `cached.scheduleInterval` | 300        | Prevents buffer exhaustion during sustained low-traffic periods |
| `bid.initialMapCapacity`  | 512        | Set to the approximate number of distinct `bizKey` values       |

---

## 6. Integration Guide

### 6.1 Prerequisites

**Step 1** — Add Starter dependency:

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.idgen-starter</artifactId>
  <version>3.0.0</version>
</dependency>
```

**Step 2** — Enable auto-configuration:

```yaml
angus:
  idgen:
    enabled: true
```

**Step 3** — Configure data source (JPA entity scan + DDL auto-init):

Script files are located at:

- `idgen/core/src/main/resources/schema/mysql/idgen-schema.sql`
- `idgen/core/src/main/resources/schema/postgres/idgen-schema.sql`

```yaml
angus:
  datasource:
    extra:
      entityPackages[0]: cloud.xcan.angus.idgen.entity
    mysql:
      schema[0]: schema/mysql/idgen-schema.sql   # Auto-executes on startup if tables are missing
    postgresql:
      schema[0]: schema/postgres/idgen-schema.sql
```

### 6.2 Scenario 1: Generate a 64-bit Distributed ID (UidGenerator)

Suitable for database primary keys, distributed trace IDs, and any scenario requiring
high-throughput unique numeric IDs.

**Inject in service layer**:

```java
@Service
public class OrderService {

    @Resource
    private CachedUidGenerator uidGenerator;

    public Order createOrder() {
        long uid = uidGenerator.getUID();
        // uid is a globally unique, time-ordered 64-bit long
        Order order = new Order();
        order.setId(uid);
        return orderRepository.save(order);
    }
}
```

**Parse a UID (for debugging)**:

```java
String parsed = uidGenerator.parseUID(uid);
// Example: {"UID":"6765612809367875584","timestamp":"2024-09-01 10:30:00",
//           "workerId":"1","sequence":"0"}
```

**Obtain outside a Spring-managed bean**:

```java
CachedUidGenerator generator =
    (CachedUidGenerator) SpringContextHolder.getBean("uidGenerator");
long uid = generator.getUID();
```

### 6.3 Scenario 2: Generate a Readable Business ID — DB Mode

Suitable for order numbers, contract codes, and similar documents that are customer-facing.

**Step 1** — Insert a config row into `id_config`:

```sql
-- PREFIX_DATE_SEQ format | DB mode | platform-wide unique | step = 5000
INSERT INTO `id_config`
  (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`,
   `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `modified_date`)
VALUES
  (UUID(), 'order', 'PREFIX_DATE_SEQ', 'ORD', 'YYYYMMDD', 10,
   'DB', 'PLATFORM', -1, 0, 5000, NOW(), NOW());
```

Generated example: `ORD202409010000000001` (prefix `ORD` + date `20240901` + 10-digit sequence)

**Step 2** — Inject and call:

```java
@Service
public class OrderService {

    @Resource
    private BidGenerator bidGenerator;

    public String generateOrderNo() {
        return bidGenerator.getId("order");
        // Returns: ORD202409010000000001
    }

    public List<String> generateBatch(int count) {
        return bidGenerator.getIds("order", count);
        // Fetches one or a few segments internally; highly efficient
    }
}
```

### 6.4 Scenario 3: Generate a Business ID — Redis Mode

Suitable for multi-instance deployments that require strong consistency without local segment
caching.

**Enable Redis**:

```yaml
angus:
  idgen:
    enabled: true
xcan:
  redis:
    enabled: true
```

**Insert config row** (Redis mode; `step` is ignored):

```sql
-- DATE_SEQ format | Redis mode | platform-wide unique
INSERT INTO `id_config`
  (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`,
   `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `modified_date`)
VALUES
  (UUID(), 'invoice', 'DATE_SEQ', '', 'YYYYMMDD', 8,
   'REDIS', 'PLATFORM', -1, 0, 1, NOW(), NOW());
```

```java
String invoiceNo = bidGenerator.getId("invoice");
// Returns: 2024090100000001
```

### 6.5 Scenario 4: Multi-Tenant Business IDs (TENANT Scope)

Each tenant's sequence is independent. Tenant-specific rows are auto-cloned from the template on
first use.

**Insert template row** (`tenantId = -1`):

```sql
INSERT INTO `id_config`
  (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`,
   `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `modified_date`)
VALUES
  (UUID(), 'ticket', 'PREFIX_SEQ', 'TK', '', 8,
   'DB', 'TENANT', -1, 0, 2000, NOW(), NOW());
```

```java
// Different tenants get independent sequences
String ticket1 = bidGenerator.getId("ticket", 1001L);  // TK00000001 (tenant 1001)
String ticket2 = bidGenerator.getId("ticket", 2002L);  // TK00000001 (tenant 2002, independent)
```

### 6.6 Scenario 5: Batch ID Generation

```java
// Fetch 100 order numbers (one or few segment fetches internally)
List<String> orderNos = bidGenerator.getIds("order", 100);

// Batch with tenant scope
List<String> ticketNos = bidGenerator.getIds("ticket", 50, 1001L);
```

> Batch limit: `BidGenerator.MAX_BATCH_NUM = 10,000`.

### 6.7 Custom WorkerId Assigner (Optional)

The default `DisposableInstanceIdAssigner` assigns workerId via the `angus_instance` table. Override
it with a custom implementation (e.g., ZooKeeper-based):

```java
@Bean
@ConditionalOnMissingBean
public InstanceIdAssigner myInstanceIdAssigner() {
    return new InstanceIdAssigner() {
        @Override
        public long assignInstanceIdByEnv() {
            // Read unique node ID from ZooKeeper / Consul / Config Center
            return zkClient.getNodeId();
        }

        @Override
        public long assignInstanceIdByParam(String host, String port, String type) {
            return zkClient.getNodeId(host, port);
        }
    };
}
```

> Because the framework uses `@ConditionalOnMissingBean`, a user-defined bean automatically replaces
> the default implementation.

---

## 7. Performance

| Generator             | Mode           | Single-Instance QPS | Notes                                           |
|-----------------------|----------------|---------------------|-------------------------------------------------|
| `DefaultUidGenerator` | Synchronous    | ~500K               | `synchronized nextId()`; bounded by clock tick  |
| `CachedUidGenerator`  | RingBuffer     | **~6M**             | Lock-free consume; produce/consume parallelized |
| `BidGenerator`        | DB (step=5000) | ~100K–500K          | Depends on segment size and DB latency          |
| `BidGenerator`        | Redis          | ~200K–1M            | One `INCRBY` per call; depends on Redis latency |

- [UidGenerator Performance Details](performance/UIDPerformance.md)
- [BidGenerator Performance Details](performance/BIDPerformance.md)

---

## 8. Important Notes

1. **Bit allocation is immutable at runtime**: Changing `timeBits`/`workerBits`/`seqBits` after
   go-live corrupts existing ID parsing and requires full data migration.
2. **Epoch change risk**: Modifying `epochStr` causes ID generation to restart from a reduced delta
   value, potentially overlapping with historical IDs.
3. **`angus_instance` table unique constraint**: The same `host:port` reuses its workerId on
   restart. A new deployment address generates a new row.
4. **`CachedUidGenerator` memory**: At `boostPower=2`, the RingBuffer occupies ~256
   KB (`32,768 × 8 bytes`). Adjust for your environment.
5. **BidGenerator segment loss**: In-memory segments are lost on crash or restart, causing gaps in
   IDs. This is expected behavior.
6. **TENANT scope first-call latency**: The first call for a new tenant triggers one DB write to
   clone the template row. Subsequent calls use the cached config.
7. **`angus_id_config` table auto-created**: Controlled by `xcan.datasource.mysql.schema`
   configuration; no manual DDL required.
8. **Never manually update `max_id`**: Direct modification under concurrent use can cause duplicate
   IDs.
