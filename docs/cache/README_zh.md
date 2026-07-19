# 混合分布式缓存（angus-infra-cache）

[English](README.md) | [中文](README_zh.md)

提供 **L1 进程内 Caffeine + L2 持久化（默认 JPA）** 的字符串 KV 缓存，供业务与安全模块
（如分布式 Token）跨实例共享、重启后恢复。L2 写失败会降级，不影响 L1 可用性。

> **不适合：** Spring `@Cacheable` 对象缓存（请用 `l2cache` / Redis）；高一致性强共享场景
> （多实例无 L1 失效广播，请用 Redis）。值类型仅为 `String`。

---

## 目录

1. [概述](#1-概述)
2. [架构](#2-架构)
3. [数据模型](#3-数据模型)
4. [配置项](#4-配置项)
5. [接入指南](#5-接入指南)
6. [API 参考](#6-api-参考)
7. [缓存与一致性](#7-缓存与一致性)
8. [管理 REST](#8-管理-rest)
9. [最佳实践](#9-最佳实践)
10. [排查指南](#10-排查指南)

---

## 1. 概述

| 组件 | 职责 |
|------|------|
| `IDistributedCache` | 主 API：读写 / TTL / 统计 / 清理 |
| `HybridCacheManager` | L1 + L2 协调 |
| `CaffeineMemoryCache` | L1 内存缓存（per-entry TTL） |
| `CachePersistence` | L2 抽象；默认 JPA → `angus_cache_entries` |
| `TransactionalDistributedCache` | 写/读事务装饰 |
| `cache-web` 管理 REST | 可选运维接口（非 Actuator） |

**设计原则**

1. **L1 极速** — Caffeine，亚毫秒命中、LRU、按条目 TTL。
2. **L2 耐久/共享** — 默认 Spring Data JPA；可自定义 `CachePersistence`。
3. **降级容错** — L2 写失败仅打 `[CACHE-DEGRADATION]`，L1 仍可用。
4. **事务安全** — 默认包装 `@Transactional`。
5. **零侵入** — Starter 自动装配；管理 API 按需引入 `cache-web`。

---

## 2. 架构

```
App → IDistributedCache
        └─ TransactionalDistributedCache (@Transactional)
              └─ HybridCacheManager
                    ├─ L1: CaffeineMemoryCache
                    └─ L2: CachePersistence
                          ├─ SpringCachePersistenceAdapter → DB
                          └─ NoOpCachePersistence（无 JPA 时回退）
```

**读路径**

1. 查 L1；命中且未过期 → 返回。
2. L1 miss → 查 L2；未过期则回填 L1 后返回。
3. L2 已过期 → 返回空（**懒删除**，不在 get 路径删库）。

**写路径**

1. 写入 L1。
2. Best-effort 写入 L2（失败不抛给调用方）。

---

## 3. 数据模型

### 3.1 表 `angus_cache_entries`

脚本：`classpath:schema/mysql/cache-schema.sql`（Postgres：`schema/postgres/`）

| 列 | 类型 | 必填 | 说明 |
|----|------|------|------|
| `id` | BIGINT | 是 | 主键 |
| `cache_key` | VARCHAR(256) | 是 | 唯一键 |
| `cache_value` | LONGTEXT | 是 | 字符串值 |
| `created_at` / `updated_at` | DATETIME | 是 | 创建/更新时间 |
| `expire_at` | DATETIME | 否 | 过期时间；`NULL` 表示永不过期 |
| `ttl_seconds` | BIGINT | 否 | 写入时 TTL |
| `is_expired` | BOOLEAN | 否 | 标记列；运行时过期判断以 `expire_at` 为准 |

**唯一索引：** `uk_cache_key`（`cache_key`）  
**辅助索引：** `idx_expire_time`（`expire_at`）

### 3.2 实体

`cloud.xcan.angus.cache.entity.CacheEntry` → `@Table(name = "angus_cache_entries")`

---

## 4. 配置项

前缀：`angus.cache`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `memory.max-size` | `10000` | L1 最大条目（LRU） |
| `memory.cleanup-interval-seconds` | `300` | **兼容保留**；不再驱动 Caffeine 驱逐 |
| `management.enabled` | `false` | 属性存在；当前代码**未读取**（见 §8） |

```yaml
angus:
  cache:
    memory:
      max-size: 10000
      cleanup-interval-seconds: 300
    management:
      enabled: false

spring:
  jpa:
    hibernate:
      ddl-auto: validate
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/mysql/cache-schema.sql
```

Angus 产品常见写法：在 `angus.datasource.extra.entityPackages` 中加入
`cloud.xcan.angus.cache.entity`，并在 schema 列表中挂载 `cache-schema.sql`。

---

## 5. 接入指南

### 5.1 依赖

**编程 API（多数应用）：**

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.cache-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

**需要管理 REST 时再加：**

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.cache-web</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

并确保组件扫描覆盖 `cloud.xcan.angus.cache.web`（Angus 应用通常
`scanBasePackages = "cloud.xcan.angus"`）。

> 勿与 `xcan-angusinfra.l2cache-*`（Redis + Caffeine 的 Spring Cache）混淆。

### 5.2 自动装配

`HybridCacheAutoConfiguration`（`@ConditionalOnClass(JpaRepository.class)`）：

| Bean | 条件 | 说明 |
|------|------|------|
| `CachePersistence` | `@ConditionalOnMissingBean` | 有 JPA Repo → 适配器；否则 `NoOpCachePersistence` |
| `IDistributedCache` | `@ConditionalOnMissingBean` | `TransactionalDistributedCache` 包装 `HybridCacheManager` |

自定义 L2：实现 `CachePersistence` 并注册为 `@Bean`，即可覆盖默认实现。

### 5.3 业务调用

```java
@Service
public class TokenService {
  private final IDistributedCache cache;

  public TokenService(IDistributedCache cache) {
    this.cache = cache;
  }

  public String getOrLoad(String key) {
    return cache.get(key).orElseGet(() -> {
      String value = loadFromDb(key);
      cache.set(key, value, 300L); // TTL 秒；null = 永不过期
      return value;
    });
  }
}
```

### 5.4 定时清理过期 L2

```java
@Scheduled(fixedRate = 3_600_000)
public void cleanupExpired() {
  cache.cleanupExpiredEntries();
}
```

生产环境务必定期 `cleanup`，避免表膨胀（get 路径不删过期行）。

---

## 6. API 参考

### `IDistributedCache`

| 方法 | 说明 |
|------|------|
| `set(key, value, ttlSeconds)` | 写入；`ttlSeconds=null` 永不过期 |
| `set(key, value)` | 永不过期写入 |
| `get(key)` | `Optional<String>` |
| `delete(key)` / `exists(key)` | 删除 / 是否存在且未过期 |
| `getTTL(key)` | `-1` 无过期；`-2` 不存在/已过期（看 L2） |
| `expire(key, ttlSeconds)` | 更新 TTL 并失效 L1 |
| `clear()` | 清空 L1+L2 |
| `getStats()` | 命中率等统计 |
| `cleanupExpiredEntries()` | 清理过期 L2，返回删除数 |
| `listEntries()` / `listEntries(page, size)` | 活跃条目列表（不含 value） |

### `CachePersistence`（扩展点）

`findByKey` / `save` / `deleteByKey` / `deleteAll` / `count` /
`countExpiredEntries` / `deleteExpiredEntries` / `findAllActive` / `findAllActive(Pageable)`

---

## 7. 缓存与一致性

- **多实例：** 无 L1 失效广播。实例 A 更新后，实例 B 的 L1 可能脏读，直到 TTL 过期再从 L2 加载。
- **降级：** L2 写失败时数据仅留在本机内存；DB 恢复后需业务重建或等待过期。
- **`getTTL` / `expire`：** 以 L2 为准；仅 L1 有数据时 `getTTL` 可能返回 `-2`。
- **读不刷新 TTL：** 与 Redis GET 语义一致。

---

## 8. 管理 REST

模块：`xcan-angusinfra.cache-web`  
前缀：`/api/v1/cache`  
鉴权：`@PreAuthorize("@PPS.isCloudTenantSecurity() && @PPS.isSysAdmin()")`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/stats` | 聚合统计 |
| GET | `/entries?pageNo=&pageSize=` | 分页活跃条目（不含 value） |
| GET / PUT / DELETE | `/{key}` | 读 / 写 / 删 |
| GET | `/{key}/exists` | 是否存在 |
| GET | `/{key}/ttl` | TTL |
| POST | `/{key}/expire` | Body: `{"ttlSeconds":120}` |
| POST | `/clear` | 清空（审计日志 `[CACHE-AUDIT]`） |
| POST | `/cleanup` | 清理过期 L2 |

Key 约束：非空、≤ 256。  
**注意：** `angus.cache.management.enabled` 当前**不会**控制 Controller 注册；引入
`cache-web` + 组件扫描即暴露，务必靠 Security 保护。

---

## 9. 最佳实践

1. 仅缓存可接受短暂不一致的配置/会话类字符串数据。
2. 多实例强一致场景改用 Redis / `l2cache`，或设置较短 TTL。
3. 生产必须定时 `cleanupExpiredEntries()`。
4. 管理 REST 仅在可信环境暴露，并限制为系统管理员。
5. Key 命名加业务前缀，避免冲突（如 `token:{id}`）。
6. 自定义 L2 时实现完整 `CachePersistence` 契约（含分页活跃列表）。

---

## 10. 排查指南

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 多实例读到旧值 | L1 未失效 | 缩短 TTL 或改用 Redis |
| DB 有数据但 get 为空 | 已过期 / 时区问题 | 查 `expire_at`；核对时钟 |
| DB 无数据但本机可读 | L2 降级写入失败 | 查 `[CACHE-DEGRADATION]` 日志 |
| 表持续膨胀 | 未做过期清理 | 加定时 `cleanupExpiredEntries` |
| 管理 API 404 | 未引入 `cache-web` 或未扫描 | 加依赖并扫描 `cloud.xcan.angus` |
| 管理 API 403 | 无 PPS / 非系统管理员 | 检查 Security 与租户上下文 |

---

## 相关类

| 类 | 模块 |
|----|------|
| `cloud.xcan.angus.cache.IDistributedCache` | `cache-core` |
| `cloud.xcan.angus.cache.HybridCacheManager` | `cache-core` |
| `cloud.xcan.angus.cache.CachePersistence` | `cache-core` |
| `cloud.xcan.angus.cache.autoconfigure.HybridCacheAutoConfiguration` | `cache-starter` |
| `cloud.xcan.angus.cache.web.CacheManagementController` | `cache-web` |
