# Distributed ID Generation (angus-infra-idgen)

[English](README.md) | [中文](README_zh.md)

Baidu [UidGenerator](https://github.com/baidu/uid-generator)-based ID toolkit with **UID**
(64-bit Snowflake variant) and **BID** (human-readable business codes), JPA multi-DB support, and
Spring Boot auto-configuration.

> **Not for:** strictly contiguous sequences without gaps (segment leftovers are discarded on
> restart), or designs that forbid local segment caching.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Data Model](#3-data-model)
4. [Configuration](#4-configuration)
5. [Integration Guide](#5-integration-guide)
6. [API Reference](#6-api-reference)
7. [Instance ID Assignment](#7-instance-id-assignment)
8. [Performance Notes](#8-performance-notes)
9. [Best Practices](#9-best-practices)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Overview

| | **UidGenerator** | **BidGenerator** |
|--|------------------|------------------|
| Output | `long` | `String` |
| Use cases | PK, trace, high-throughput IDs | Order/contract/ticket numbers |
| State | RingBuffer + `angus_instance` | `angus_id_config` + local segment / Redis |
| Default impl | `CachedUidGenerator` | `DefaultBidGenerator` |

**Default UID bit layout (code authority):**

```
sign(1) | deltaSeconds(32) | workerId(13) | sequence(18) = 64
epoch = 2021-01-01
```

**Design principles:** stable bit layout after go-live; disposable workerId (+1 each restart);
segmented BID allocation (gaps OK); TENANT scope can clone from `tenantId=-1` templates.

---

## 2. Architecture

**UID:** assign workerId → init RingBuffer → `getUID()` takes from buffer (fallback sync `nextId`).  
**BID:** load `IdConfig` → DB `max_id+=step` or Redis `INCRBY(key, step)` → local `AtomicLong` →
format string. Redis mode still uses local segments (not remote +1 per ID).

---

## 3. Data Model

Scripts: `classpath:schema/mysql/idgen-schema.sql` (Postgres sibling).

- `angus_instance` — worker assignment; UNIQUE(`host`,`port`); `id` has **no** AUTO_INCREMENT
- `angus_id_config` — BID format/mode/scope/step; UNIQUE(`biz_key`,`tenant_id`)

---

## 4. Configuration

Prefix: `angus.idgen`  
`CoreCondition` requires **explicit** `angus.idgen.enabled=true` in the environment.

| Property | Default | Notes |
|----------|---------|-------|
| `enabled` | field `true` | Must be explicitly true to load |
| `uid.timeBits` / `workerBits` / `seqBits` | `32` / `13` / `18` | Must sum to 63 with sign |
| `uid.epochStr` | `2021-01-01` | `yyyy-MM-dd` |
| `uid.retriesNum` | `3` | InstanceId retries |
| `cached.boostPower` | `2` | Buffer size boost |
| `cached.paddingFactor` | `50` | Declared but **not wired** |
| `cached.scheduleInterval` | `300` | Scheduled padding seconds |
| `cached.rejectionPolicy` | `BLOCK` | `BLOCK`/`DISCARD`/`EXCEPTION` |
| `bid.initialMapCapacity` | `512` | Only bid field that takes effect |
| `bid.maxStep` / `maxBatchNum` / `maxSeqLength` | present | Runtime uses interface constants |

Also required: `info.app.runtime`. For Redis BID mode: `RedisTemplate<String, Object>`.

---

## 5. Integration Guide

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.idgen-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

Scan entities `cloud.xcan.angus.idgen.entity`, run schema SQL, set `angus.idgen.enabled=true`.

```java
@Resource
private CachedUidGenerator cachedUidGenerator; // bean name: cachedUidGenerator

@Resource
private BidGenerator bidGenerator;

long uid = cachedUidGenerator.getUID();
String orderNo = bidGenerator.getId("order");
```

Override `InstanceIdAssigner` for custom workerId strategies.

---

## 6. API Reference

`UidGenerator`: `getUID()`, `parseUID(long)`.  
`BidGenerator`: `getId`, `getIds` (+ tenant overloads).  
Enums: `Format`, `Mode`, `Scope`, `DateFormat`.  
Exception: `IdGenerateException`.

---

## 7. Instance ID Assignment

Default `DisposableInstanceIdAssigner`: existing `(host,port)` → `id = id + 1`; new → `id = 1`.  
Same host:port **consumes a new workerId every restart**. High-churn restarts need larger
`workerBits`. Multiple first-boot hosts may all get `id=1` — use a custom assigner in production
if that collision risk is unacceptable.

---

## 8. Performance Notes

See `performance/UIDPerformance.md` and `BIDPerformance.md` in the module. Cached UID can reach
millions QPS; BID benefits from `step` in the 1k–10k range.

---

## 9. Best Practices

1. Never change bit widths / epoch after production IDs exist.
2. Size `workerBits` for restart churn; `timeBits` for lifetime.
3. Keep BID `step` between 1k–10k; `batchNum ≤ step`.
4. Seed TENANT templates with `tenant_id=-1`.
5. Never hand-edit `max_id`.
6. Accept BID gaps as normal.
7. Config prefix is `angus.idgen` (not `xcan.idgen`).

---

## 10. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Beans missing | `enabled` not explicit | Set `angus.idgen.enabled=true` |
| InstanceId failure | DB/schema | Create tables / check DS |
| Clock moved backwards | NTP skew | Fix clock |
| Redis BID fails | No RedisTemplate | Provide bean or use `DB` |
| Config not found | Missing seed row | Insert `angus_id_config` |
| seqLength overflow | Sequence too wide | Increase `seq_length` |
| Suspected UID clash | Multiple workers with id=1 | Custom `InstanceIdAssigner` |

---

## Related classes

| Class | Module |
|-------|--------|
| `cloud.xcan.angus.idgen.UidGenerator` | `idgen-core` |
| `cloud.xcan.angus.idgen.uid.CachedUidGenerator` | `idgen-core` |
| `cloud.xcan.angus.idgen.BidGenerator` | `idgen-core` |
| `cloud.xcan.angus.idgen.autoconfigure.IdGenAutoConfiguration` | `idgen-starter` |
| `cloud.xcan.angus.persistence.jpa.identity.SnowflakeIdGenerator` | `jpa-core` |
