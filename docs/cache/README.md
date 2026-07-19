# Hybrid Distributed Cache (angus-infra-cache)

[English](README.md) | [中文](README_zh.md)

A **L1 in-process Caffeine + L2 durable (default JPA)** string KV cache for sharing
configuration/session data across instances and surviving restarts. L2 write failures degrade
gracefully without breaking L1.

> **Not for:** Spring `@Cacheable` object caching (use `l2cache` / Redis); strong cross-instance
> consistency (no L1 invalidation broadcast). Values are `String` only.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Data Model](#3-data-model)
4. [Configuration](#4-configuration)
5. [Integration Guide](#5-integration-guide)
6. [API Reference](#6-api-reference)
7. [Caching & Consistency](#7-caching--consistency)
8. [Management REST](#8-management-rest)
9. [Best Practices](#9-best-practices)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Overview

| Piece | Responsibility |
|-------|----------------|
| `IDistributedCache` | Primary API: get/set/TTL/stats/cleanup |
| `HybridCacheManager` | Coordinates L1 + L2 |
| `CaffeineMemoryCache` | L1 with per-entry TTL |
| `CachePersistence` | L2 SPI; default JPA → `angus_cache_entries` |
| `TransactionalDistributedCache` | Transactional decorator |
| `cache-web` REST | Optional ops API (not Actuator) |

**Design principles**

1. **Fast L1** — Caffeine, sub-ms hits, LRU, per-entry TTL.
2. **Durable/shared L2** — Spring Data JPA by default; replaceable via `CachePersistence`.
3. **Degrade on L2 failure** — log `[CACHE-DEGRADATION]`; keep serving from L1.
4. **Transactional writes** — wrapped with `@Transactional`.
5. **Zero-touch starter** — auto-config; ops REST via optional `cache-web`.

---

## 2. Architecture

```
App → IDistributedCache
        └─ TransactionalDistributedCache (@Transactional)
              └─ HybridCacheManager
                    ├─ L1: CaffeineMemoryCache
                    └─ L2: CachePersistence
                          ├─ SpringCachePersistenceAdapter → DB
                          └─ NoOpCachePersistence (fallback)
```

**Read path:** L1 hit → return; miss → L2; warm L1 if not expired; expired → empty (lazy delete).  
**Write path:** write L1, then best-effort L2.

---

## 3. Data Model

### 3.1 Table `angus_cache_entries`

Scripts: `classpath:schema/mysql/cache-schema.sql` (Postgres under `schema/postgres/`).

| Column | Type | Required | Description |
|--------|------|----------|-------------|
| `id` | BIGINT | Yes | PK |
| `cache_key` | VARCHAR(256) | Yes | Unique key |
| `cache_value` | LONGTEXT | Yes | String value |
| `created_at` / `updated_at` | DATETIME | Yes | Timestamps |
| `expire_at` | DATETIME | No | Expiry; `NULL` = never |
| `ttl_seconds` | BIGINT | No | TTL at write |
| `is_expired` | BOOLEAN | No | Marker; runtime uses `expire_at` |

Unique index on `cache_key`; index on `expire_at`.

---

## 4. Configuration

Prefix: `angus.cache`

| Property | Default | Description |
|----------|---------|-------------|
| `memory.max-size` | `10000` | L1 max entries (LRU) |
| `memory.cleanup-interval-seconds` | `300` | Retained for compatibility; does **not** drive Caffeine eviction |
| `management.enabled` | `false` | Present but **not read** by current code (see §8) |

```yaml
angus:
  cache:
    memory:
      max-size: 10000
    management:
      enabled: false
```

---

## 5. Integration Guide

### 5.1 Dependencies

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.cache-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

Optional ops API: `xcan-angusinfra.cache-web` + component scan of `cloud.xcan.angus.cache.web`.

Do **not** confuse with `xcan-angusinfra.l2cache-*` (Redis-backed Spring Cache).

### 5.2 Auto-configuration

`HybridCacheAutoConfiguration` creates `CachePersistence` + `IDistributedCache` when missing.
Provide a custom `CachePersistence` bean to override L2.

### 5.3 Usage

```java
cache.set("token:1", value, 300L);
Optional<String> v = cache.get("token:1");
```

Schedule `cleanupExpiredEntries()` in production to prevent table growth.

---

## 6. API Reference

### `IDistributedCache`

`set` / `get` / `delete` / `exists` / `getTTL` / `expire` / `clear` / `getStats` /
`cleanupExpiredEntries` / `listEntries` / `listEntries(page, size)`.

`getTTL`: `-1` no expiry; `-2` missing/expired (L2 view).

---

## 7. Caching & Consistency

- Multi-instance: no L1 invalidation broadcast — stale until TTL.
- L2 write failure: data stays in local memory only.
- `getTTL` / `expire` consult L2; L1-only entries may report `-2`.

---

## 8. Management REST

Module: `cache-web`, prefix `/api/v1/cache`, secured by `PPS` sys-admin check.

Endpoints: `/stats`, `/entries`, `/{key}`, `/{key}/exists`, `/{key}/ttl`, `/{key}/expire`,
`/clear`, `/cleanup`.

`angus.cache.management.enabled` does **not** gate controller registration today — protect with Security.

---

## 9. Best Practices

1. Cache only soft-consistent string data.
2. Prefer Redis/`l2cache` for strong consistency, or short TTLs.
3. Always schedule L2 expired cleanup.
4. Expose management REST only in trusted environments.
5. Prefix keys by domain (`token:{id}`).

---

## 10. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Stale multi-instance reads | L1 not invalidated | Shorter TTL or Redis |
| get empty with DB row | Expired / clock skew | Check `expire_at` |
| Local hit but no DB row | L2 degradation | Check `[CACHE-DEGRADATION]` logs |
| Table growth | No cleanup | Schedule `cleanupExpiredEntries` |
| Management 404 | Missing `cache-web` / scan | Add dep + scan `cloud.xcan.angus` |

---

## Related classes

| Class | Module |
|-------|--------|
| `cloud.xcan.angus.cache.IDistributedCache` | `cache-core` |
| `cloud.xcan.angus.cache.HybridCacheManager` | `cache-core` |
| `cloud.xcan.angus.cache.CachePersistence` | `cache-core` |
| `cloud.xcan.angus.cache.autoconfigure.HybridCacheAutoConfiguration` | `cache-starter` |
| `cloud.xcan.angus.cache.web.CacheManagementController` | `cache-web` |
