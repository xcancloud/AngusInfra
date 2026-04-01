# AngusInfra — Cache Module

## Overview

The `cache` module implements a hybrid two-level cache for AngusInfra combining in-memory caching
and a persistent backing store. It is designed to provide:

- Fast in-memory operations for hot data
- Persistent storage for durability and sharing across instances (via JPA)
- Unified management APIs for monitoring and administration
- Transactional semantics via a proxy wrapper for safe persistence operations

## Module Layout

- `core` — Core cache interfaces and implementations (
  e.g. `HybridCacheManager`, `CaffeineMemoryCache`, `CachePersistence` interfaces).
- `starter` — Spring Boot starter providing auto-configuration, optional Spring Data JPA persistence
  adapter and management REST controllers.

## Key Interfaces / Classes

- `cloud.xcan.angus.cache.IDistributedCache` — Public cache API used by applications.
- `cloud.xcan.angus.cache.HybridCacheManager` — Core implementation combining memory cache and
  persistence.
- `cloud.xcan.angus.cache.CaffeineMemoryCache` — L1 in-memory cache powered by Caffeine.
- `cloud.xcan.angus.cache.CachePersistence` — Persistence abstraction for storing cache entries.
- `cloud.xcan.angus.cache.entry.CacheEntry` / `CacheEntryRepository` — JPA entity and repository for
  persisted entries. The backing table is named **`angus_cache_entries`**.
- `cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository` — Spring Data repository (starter).
- `cloud.xcan.angus.cache.autoconfigure.HybridCacheAutoConfiguration` — Auto-configuration that
  wires persistence and management controller when appropriate.
- `cloud.xcan.angus.cache.autoconfigure.TransactionalDistributedCache` — Decorator that adds Spring
  `@Transactional` semantics to every cache operation.
- `cloud.xcan.angus.cache.autoconfigure.NoOpCachePersistence` — Pure in-memory fallback (no DB).
- `cloud.xcan.angus.cache.web.CacheManagementController` — Management REST controller
  exposing monitoring and admin endpoints (enabled via `angus.cache.management.enabled=true`).

## Architecture

The module uses a two-level (L1 + L2) hybrid cache strategy:

| Level | Implementation | Characteristics |
|-------|---------------|-----------------|
| L1 (memory) | Caffeine | Sub-millisecond reads, LRU eviction, per-entry TTL, built-in stats |
| L2 (persistence) | Spring Data JPA / `ConcurrentHashMap` fallback | Cross-instance sharing, durable across restarts, batch expiry cleanup |

**Read path:** L1 hit → return immediately. L1 miss → query L2 → if valid, warm L1 then return.

**Write path:** Always write L1 first (fast path), then attempt L2 (best-effort; DB failures degrade
gracefully to memory-only mode without throwing exceptions).

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
  <artifactId>xcan-angusinfra.cache-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

### 3. Inject and use

```java
@Service
public class MyService {
    private final IDistributedCache cache;

    public MyService(IDistributedCache cache) {
        this.cache = cache;
    }

    public String get(String key) {
        return cache.get(key).orElseGet(() -> {
            String value = loadFromDatabase(key);
            cache.set(key, value, 300L); // cache for 5 minutes
            return value;
        });
    }
}
```

### 4. Persistence (optional)

The starter auto-configures a `CachePersistence` adapter when a
`SpringDataCacheEntryRepository` bean is present (i.e. you include the JPA starter and scan the
package). Otherwise the cache runs with **`NoOpCachePersistence`** (pure in-memory via
`ConcurrentHashMap`) — data is not durable across restarts.

If you enable JPA persistence, configure a datasource in `application.yml` and scan the cache
module's packages:

```java
@SpringBootApplication
@EntityScan(basePackages = {
    "com.yourcompany.app",
    "cloud.xcan.angus.cache.entry"   // required: CacheEntry entity
})
@EnableJpaRepositories(basePackages = {
    "com.yourcompany.app.repository",
    "cloud.xcan.angus.cache.jpa"     // required: SpringDataCacheEntryRepository
})
public class YourApplication { ... }
```

The JPA entity maps to the table **`angus_cache_entries`**. To create the table manually (recommended
for production instead of `ddl-auto: update`):

```sql
CREATE TABLE IF NOT EXISTS angus_cache_entries (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    cache_key   VARCHAR(256) NOT NULL,
    cache_value LONGTEXT,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    expire_at   DATETIME,
    ttl_seconds BIGINT,
    is_expired  BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_cache_key (cache_key),
    INDEX idx_expire_time (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 5. Configuration reference

```yaml
angus:
  cache:
    memory:
      max-size: 10000              # Max entries in L1 (LRU eviction threshold). Default: 10000
      cleanup-interval-seconds: 300  # Kept for API compatibility; Caffeine manages per-entry TTL automatically
    management:
      enabled: false               # Expose /api/v1/cache/** management endpoints. Default: false
```

### 6. Enable the management API

The management REST API is **disabled by default** for security reasons. Enable it only in trusted
environments, and always protect the endpoints with authentication (e.g. Spring Security):

```yaml
angus:
  cache:
    management:
      enabled: true
```

## Management API

The management controller is available under `/api/v1/cache` (requires
`angus.cache.management.enabled=true`).

> **Security notice:** These endpoints can read, write, and clear all cached data. Always protect
> them with authentication before enabling in non-local environments.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/cache/stats` | Aggregated stats: entry counts, hit rate, memory size, etc. |
| `GET` | `/api/v1/cache/{key}` | Get value; returns business-error wrapper when key is not found |
| `PUT` | `/api/v1/cache/{key}` | Set value. Body: `{"value":"…","ttlSeconds":60}` |
| `DELETE` | `/api/v1/cache/{key}` | Delete a key (idempotent) |
| `GET` | `/api/v1/cache/{key}/exists` | Check if key exists and is not expired |
| `GET` | `/api/v1/cache/{key}/ttl` | TTL in seconds: -1 = no expiry, -2 = not found |
| `POST` | `/api/v1/cache/{key}/expire` | Set TTL for existing key. Body: `{"ttlSeconds":120}` |
| `POST` | `/api/v1/cache/clear` | Clear all entries (memory + persistence) |
| `POST` | `/api/v1/cache/cleanup` | Delete expired entries from persistence; returns deleted count |

Key constraints: must not be blank, max 256 characters (HTTP 400 otherwise).

Example — set a value via curl:

```bash
curl -X PUT -H "Content-Type: application/json" \
  -d '{"value":"hello","ttlSeconds":60}' \
  http://localhost:8080/api/v1/cache/my-key
```

## Custom Persistence Backend

To plug in a different backend (e.g. Redis, MongoDB), implement `CachePersistence` and register it
as a Spring bean. The `@ConditionalOnMissingBean(CachePersistence.class)` guard in the
auto-configuration ensures your bean takes precedence with zero conflicts:

```java
@Component
public class RedisCachePersistence implements CachePersistence {
    // implement all methods ...
}
```

## Scheduled Cleanup (JPA mode)

In JPA mode, schedule periodic cleanup to prevent unbounded table growth:

```java
@Scheduled(fixedRate = 3_600_000) // every hour
public void cleanup() {
    int deleted = cache.cleanupExpiredEntries();
    log.info("Cache cleanup: deleted {} expired entries", deleted);
}
```

## Building and Deploying

- Include the starter in your Spring Boot service to enable management endpoints and optional JPA
  persistence support.
- If using persistent store, ensure the `entry` JPA entity is scanned and the repository bean is
  available.

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
