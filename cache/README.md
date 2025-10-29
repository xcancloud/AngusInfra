# AngusInfra — Cache Module

## Overview

The `cache` module implements a hybrid two-level cache for AngusInfra combining in-memory caching and a persistent backing store. It is designed to provide:

- Fast in-memory operations for hot data
- Persistent storage for durability and sharing across instances (via JPA)
- Unified management APIs for monitoring and administration
- Transactional semantics via a proxy wrapper for safe persistence operations

## Module Layout

- `core` — Core cache interfaces and implementations (e.g. `HybridCacheManager`, `MemoryCache`, `CachePersistence` interfaces).
- `starter` — Spring Boot starter providing auto-configuration, optional Spring Data JPA persistence adapter and management REST controllers.

## Key Interfaces / Classes

- `cloud.xcan.angus.cache.IDistributedCache` — Public cache API used by applications.
- `cloud.xcan.angus.cache.HybridCacheManager` — Core implementation combining memory cache and persistence.
- `cloud.xcan.angus.cache.MemoryCache` — In-memory cache implementation.
- `cloud.xcan.angus.cache.CachePersistence` — Persistence abstraction for storing cache entries.
- `cloud.xcan.angus.cache.entry.CacheEntry` / `CacheEntryRepository` — JPA entity and repository for persisted entries.
- `cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository` — Spring Data repository (starter).
- `cloud.xcan.angus.cache.autoconfigure.HybridCacheAutoConfiguration` — Auto-configuration that wires persistence and management controller when appropriate.
- `cloud.xcan.angus.cache.management.CacheManagementController` — Management REST controller exposing monitoring and admin endpoints.

## Quick Start

### 1. Build the module

From the repository root run:

```bash
mvn -pl cache -am clean install
```

### 2. Add the starter to your Spring Boot application

Example Maven dependency:

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.cache-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

### 3. Persistence (optional)

The starter will auto-configure a `CachePersistence` adapter if a Spring Data `SpringDataCacheEntryRepository` bean is present (i.e. you include the JPA starter and repository). Otherwise the cache will run with in-memory persistence stub behavior.

If you enable JPA persistence, configure a datasource in `application.yml`.

## Management API

The management controller is available under `/api/v1/cache` and provides the following endpoints:

- `GET /api/v1/cache/stats` — Get aggregated cache statistics (entries, hits, misses, memory size, etc.)
- `GET /api/v1/cache/{key}` — Get cache value for a key (returns business error wrapper if key not found)
- `PUT /api/v1/cache/{key}` — Set value for a key (JSON body: value, optional `ttlSeconds`)
- `DELETE /api/v1/cache/{key}` — Delete a cache entry
- `GET /api/v1/cache/{key}/exists` — Check if key exists
- `GET /api/v1/cache/{key}/ttl` — Get TTL in seconds for key (-1 = no timeout, -2 = not found)
- `POST /api/v1/cache/{key}/expire` — Set TTL for an existing key (JSON body `ttlSeconds`)
- `POST /api/v1/cache/clear` — Clear all cache entries (memory + persistence)
- `POST /api/v1/cache/cleanup` — Remove expired entries from persistence and return deleted count

Example: set a cache value via curl

```bash
curl -X PUT -H "Content-Type: application/json" -d '{"value":"hello","ttlSeconds":60}' http://localhost:8080/api/v1/cache/my-key
```

## Building and Deploying

- Include the starter in your Spring Boot service to enable management endpoints and optional JPA persistence support.
- If using persistent store, ensure the `entry` JPA entity is scanned and the repository bean is available.

## Testing

Run unit tests for the cache module core and starter:

```bash
mvn -pl cache/core test
mvn -pl cache/starter test
```

## Contributing

Contributions welcome. Please:

- Add unit tests covering behavior changes
- Keep APIs backward compatible where possible
