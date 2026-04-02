# AngusInfra — 缓存模块

## 一、模块概述

`cache` 模块是 AngusInfra 基础设施框架提供的**混合两级缓存**
组件，将高性能内存缓存（L1）与可配置的持久化后端（L2）有机结合，在保证极低读取延迟的同时支持数据跨实例共享和重启持久化。

### 设计目标

| 目标      | 实现方式                               |
|---------|------------------------------------|
| 极低读延迟   | Caffeine 内存缓存（亚毫秒级命中）              |
| 数据持久化   | JPA 数据库持久化（可选），重启后数据不丢失            |
| 多实例数据共享 | 数据库作为共享存储，内存缓存按需从 DB 预热            |
| 降级容错    | DB 写失败时自动降级为纯内存模式，服务不中断            |
| 事务安全    | 透明事务代理包装所有写操作                      |
| 零侵入接入   | Spring Boot Starter 自动配置，无需手写 Bean |
| 可观测性    | 内置统计 API（命中率、条目数、过期数等）             |

---

## 二、架构设计

### 模块结构

```
cache/
├── core/          # 核心接口与实现（无 Spring 依赖）
│   └── src/main/java/cloud/xcan/angus/cache/
│       ├── IDistributedCache.java          # 统一缓存操作接口
│       ├── HybridCacheManager.java         # 两级缓存协调器（核心实现）
│       ├── CaffeineMemoryCache.java        # L1：Caffeine 内存缓存
│       ├── CachePersistence.java           # L2：持久化抽象接口
│       ├── CacheStats.java                 # 统计数据模型
│       ├── config/CacheProperties.java     # 配置属性
│       └── entry/
│           ├── CacheEntry.java             # JPA 持久化实体
│           └── CacheEntryRepository.java   # 持久化仓库接口
└── starter/       # Spring Boot 自动配置与适配器
    └── src/main/java/cloud/xcan/angus/cache/
        ├── autoconfigure/
        │   ├── HybridCacheAutoConfiguration.java   # 自动配置入口
        │   ├── TransactionalDistributedCache.java  # 事务代理装饰器
        │   ├── SpringCachePersistenceAdapter.java  # JPA 适配器
        │   └── NoOpCachePersistence.java           # 纯内存降级实现
        ├── jpa/
        │   └── SpringDataCacheEntryRepository.java # Spring Data JPA 仓库
        └── web/
            └── CacheManagementController.java       # 管理 REST 控制器
```

### 两级缓存读写流程

```
                    ┌─────────────────────────────────────────┐
                    │         应用调用 IDistributedCache       │
                    └───────────────────┬─────────────────────┘
                                        │
                    ┌───────────────────▼─────────────────────┐
                    │    TransactionalDistributedCache         │
                    │       （事务代理，透明包装写操作）          │
                    └───────────────────┬─────────────────────┘
                                        │
                    ┌───────────────────▼─────────────────────┐
                    │          HybridCacheManager              │
                    │            （两级协调器）                 │
                    └──────┬────────────────────┬─────────────┘
                           │                    │
             ┌─────────────▼──────┐   ┌────────▼──────────────┐
             │  L1: Caffeine      │   │  L2: CachePersistence  │
             │  内存缓存           │   │  持久化层               │
             │  (最大 10000 条)   │   │                        │
             │  LRU 驱逐          │   │  ┌──────────────────┐  │
             │  per-entry TTL     │   │  │ SpringDataCache  │  │
             └────────────────────┘   │  │ PersistenceAdapter│ │
                                      │  └────────┬─────────┘  │
                                      │           │             │
                                      │  ┌────────▼─────────┐  │
                                      │  │  数据库           │  │
                                      │  │ (angus_cache_entries) │ │
                                      │  └──────────────────┘  │
                                      └────────────────────────┘
                                      （DB 不可用时自动降级为
                                        NoOpCachePersistence）
```

**写操作（set）流程：**

1. 先写 L1 内存缓存（快速路径，始终成功）
2. 尝试写 L2 数据库（Best-Effort，失败则记录降级日志，不抛异常）

**读操作（get）流程：**

1. 查 L1 内存缓存，命中则直接返回（亚毫秒）
2. L1 未命中，查 L2 数据库
3. L2 命中且未过期，将数据预热回 L1，再返回
4. 两级均未命中，返回 `Optional.empty()`

---

## 三、核心组件详解

### 3.1 `IDistributedCache` — 统一缓存接口

所有应用代码面向此接口编程，屏蔽底层实现细节：

```java
public interface IDistributedCache {
    void set(String key, String value, Long ttlSeconds);  // 设置含TTL的缓存
    void set(String key, String value);                   // 设置永不过期的缓存
    Optional<String> get(String key);                      // 读取缓存值
    boolean delete(String key);                            // 删除缓存键
    boolean exists(String key);                            // 判断键是否存在
    long getTTL(String key);                               // 获取剩余TTL（秒）
    boolean expire(String key, long ttlSeconds);           // 刷新/设置TTL
    void clear();                                          // 清空全部缓存
    CacheStats getStats();                                 // 获取运行时统计
    int cleanupExpiredEntries();                           // 清理持久化中的过期条目
}
```

> **TTL 返回约定：** `getTTL()` 返回 `-1` 表示key永不过期，返回 `-2` 表示key不存在或已过期。

### 3.2 `CaffeineMemoryCache` — L1 内存缓存

基于 [Caffeine](https://github.com/ben-manes/caffeine) 实现，具有以下特性：

- **LRU 驱逐**：超过 `maxSize`（默认 10000）后按最近最少使用驱逐
- **per-entry TTL**：每个 key 拥有独立的过期时间（通过 `Expiry` 接口实现），而非全局 TTL
- **读操作不重置 TTL**：访问不延长过期时间，符合 Redis 语义
- **统计埋点**：通过 `.recordStats()` 开启内置命中率、驱逐数等统计
- **时钟偏移防护**：在 Caffeine TTL 之外额外做 `isExpired()` 双重检查

### 3.3 `HybridCacheManager` — 两级缓存协调器

`IDistributedCache` 的核心实现，负责协调 L1 和 L2 之间的数据流：

- **写操作降级容错**：`set()`/`delete()`/`clear()` 的 DB 操作均被 `try-catch` 包裹，DB
  故障时内存缓存仍然可用，服务不中断，并输出 `[CACHE-DEGRADATION]` 级别的警告日志
- **懒删除策略**：`get()` 读到已过期的 DB 记录时，不在读操作内删除（避免只读事务中写 DB
  的问题），由 `cleanupExpiredEntries()` 定期批量清理
- **L1 预热**：从 DB 读到有效数据后自动填充 L1，后续访问直接命中内存

### 3.4 `CachePersistence` — L2 持久化接口

```java
public interface CachePersistence {
    Optional<CacheEntry> findByKey(String key);
    CacheEntry save(CacheEntry entry);
    boolean deleteByKey(String key);
    void deleteAll();
    long count();
    long countExpiredEntries();
    int deleteExpiredEntries();
}
```

框架提供两个实现，自动配置按条件选择：

| 实现类                             | 激活条件                                        | 说明                              |
|---------------------------------|---------------------------------------------|---------------------------------|
| `SpringCachePersistenceAdapter` | 类路径存在 `SpringDataCacheEntryRepository` Bean | 委托 Spring Data JPA 进行 DB 操作     |
| `NoOpCachePersistence`          | 默认兜底                                        | `ConcurrentHashMap` 内存存储，重启数据丢失 |

### 3.5 `CacheEntry` — 持久化实体

数据库表结构（表名 `angus_cache_entries`）：

| 字段            | 类型                    | 说明                |
|---------------|-----------------------|-------------------|
| `id`          | BIGINT AUTO_INCREMENT | 主键                |
| `cache_key`   | VARCHAR(256) UNIQUE   | 缓存键，唯一索引          |
| `cache_value` | LONGTEXT              | 缓存值（JSON 字符串）     |
| `created_at`  | DATETIME              | 创建时间              |
| `updated_at`  | DATETIME              | 最后更新时间            |
| `expire_at`   | DATETIME              | 过期时间（NULL 表示永不过期） |
| `ttl_seconds` | BIGINT                | TTL 秒数（冗余字段，便于查询） |
| `is_expired`  | BOOLEAN               | 过期标志（逻辑标记）        |

索引：`idx_cache_key`（唯一）、`idx_expire_time`（过期时间范围查询优化）

### 3.6 `TransactionalDistributedCache` — 事务代理

装饰器模式，透明地为所有 `IDistributedCache` 方法添加 Spring `@Transactional`：

- **写操作**（`set`、`delete`、`expire`、`clear`、`cleanupExpiredEntries`）：可读写事务
- **读操作**（`exists`、`getTTL`、`getStats`）：`readOnly = true` 只读事务
- **`get`**：普通事务（非只读），因为缓存预热可能触发 DB 写入

### 3.7 `HybridCacheAutoConfiguration` — 自动配置

Spring Boot
自动配置入口（通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
注册），条件装配逻辑如下：

```
classpath 存在 JpaRepository ?
    ├─ 是 → 自动扫描 CacheEntry 实体和 SpringDataCacheEntryRepository 仓库
    │       CachePersistence = SpringCachePersistenceAdapter（JPA 模式，默认）
    └─ 否 → CachePersistence = NoOpCachePersistence（纯内存模式）

IDistributedCache = TransactionalDistributedCache(HybridCacheManager)

angus.cache.management.enabled = true ?
    ├─ 是 → 注册 CacheManagementController（REST 管理 API）
    └─ 否 → 不暴露管理端点（默认）
```

通过 `@AutoConfiguration` + `@EntityScan` + `@EnableJpaRepositories` 实现零配置自动扫描，用户无需在启动类上手动添加包扫描注解。

---

## 四、配置参考

所有配置项以 `angus.cache` 为前缀，对应 `CacheProperties` 类：

```yaml
angus:
  cache:
    memory:
      max-size: 10000            # L1 内存缓存最大条目数（LRU 驱逐阈值），默认 10000
      cleanup-interval-seconds: 300  # 保留字段（兼容旧版 API），Caffeine 通过 per-entity TTL 自动管理过期，此值不再驱动驱逐
    management:
      enabled: false             # 是否启用管理 REST API（/api/v1/cache/**），默认关闭
```

---

## 五、管理 REST API

管理控制器需通过 `angus.cache.management.enabled=true` 显式开启，映射根路径为 `/api/v1/cache`。

> **安全警告：** 这些接口可读写和清空所有缓存数据，**必须**配合 Spring Security 等认证机制后才能对外暴露。

| Method   | Path                         | 说明                                         |
|----------|------------------------------|--------------------------------------------|
| `GET`    | `/api/v1/cache/stats`        | 获取缓存统计（条目数、命中率、内存大小等）                      |
| `GET`    | `/api/v1/cache/{key}`        | 读取缓存值，key 不存在时返回业务错误包装                     |
| `PUT`    | `/api/v1/cache/{key}`        | 写入缓存值，Body：`{"value":"…","ttlSeconds":60}` |
| `DELETE` | `/api/v1/cache/{key}`        | 删除缓存键（幂等）                                  |
| `GET`    | `/api/v1/cache/{key}/exists` | 判断键是否存在且未过期                                |
| `GET`    | `/api/v1/cache/{key}/ttl`    | 查询剩余 TTL，-1=永不过期，-2=不存在                    |
| `POST`   | `/api/v1/cache/{key}/expire` | 为已有键设置 TTL，Body：`{"ttlSeconds":120}`       |
| `POST`   | `/api/v1/cache/clear`        | 清空全部缓存（内存 + 持久化）                           |
| `POST`   | `/api/v1/cache/cleanup`      | 清理持久化中的过期条目，返回删除数量                         |

> **key 约束：** 不能为空、长度不超过 256 字符，否则返回 HTTP 400。

---

## 六、三方接入说明

### 6.1 场景一：纯内存模式（最简接入）

适用于：单节点部署、对重启后数据丢失无要求、不依赖数据库的场景。

**步骤 1：** 引入 Maven 依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.cache-starter</artifactId>
  <version>3.0.0</version>
</dependency>
```

**步骤 2：** 无需任何配置，starter 自动检测无 JPA 仓库，使用 `NoOpCachePersistence`（`ConcurrentHashMap`
内存存储）。

**步骤 3：** 注入并使用

```java
@Service
public class MyService {

    private final IDistributedCache cache;

    public MyService(IDistributedCache cache) {
        this.cache = cache;
    }

    public void demo() {
        // 写入缓存，TTL 60 秒
        cache.set("user:1001", "{\"name\":\"Alice\"}", 60L);

        // 读取缓存
        Optional<String> value = cache.get("user:1001");
        value.ifPresent(v -> System.out.println("命中: " + v));

        // 检查存在性
        boolean exists = cache.exists("user:1001");

        // 刷新 TTL
        cache.expire("user:1001", 120L);

        // 删除
        cache.delete("user:1001");
    }
}
```

**启动日志确认：**

```
INFO  c.x.a.cache.autoconfigure.NoOpCachePersistence - Cache running in memory-only mode (no JPA persistence configured).
```

---

### 6.2 场景二：JPA 持久化模式（默认，推荐生产使用）

适用于：多实例部署、需要跨实例共享缓存数据、重启后数据不丢失的场景。

**步骤 1：** 引入 Maven 依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.cache-starter</artifactId>
  <version>3.0.0</version>
</dependency>

<!-- 数据库驱动（以 MySQL 为例） -->
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
</dependency>
```

> Starter 已内置 `spring-boot-starter-data-jpa` 依赖，无需额外引入。

**步骤 2：** 配置数据源

Starter 通过 `@AutoConfiguration` + `@EntityScan` + `@EnableJpaRepositories` 自动扫描缓存实体和仓库，
**无需在启动类上手动添加包扫描注解**。

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC
    username: root
    password: yourpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate        # 生产环境建议使用 validate 并手动建表
    show-sql: false

angus:
  cache:
    memory:
      max-size: 10000
```

**数据库手动建表（生产推荐）：**

- MySQL：`cache/core/src/main/resources/schema/mysql/cache-schema.sql`
- PostgreSQL：`cache/core/src/main/resources/schema/postgres/cache-schema.sql`

Spring SQL 初始化配置示例：

```yaml
# MySQL
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/mysql/cache-schema.sql

# PostgreSQL
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/postgres/cache-schema.sql
```

**步骤 3：** 注入使用（与纯内存模式代码完全一致，接口不变）

```java
@Service
public class MyService {

    private final IDistributedCache cache;

    public MyService(IDistributedCache cache) {
        this.cache = cache;
    }

    public String getUserProfile(Long userId) {
        String key = "user:profile:" + userId;
        return cache.get(key).orElseGet(() -> {
            // 缓存未命中，从 DB 加载
            String profile = loadFromDatabase(userId);
            cache.set(key, profile, 300L); // 缓存 5 分钟
            return profile;
        });
    }
}
```

---

### 6.3 场景三：启用管理 REST API

**配置开启：**

```yaml
angus:
  cache:
    management:
      enabled: true
```

**配合 Spring Security 保护接口（强烈推荐）：**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/cache/**").hasRole("ADMIN")  // 仅管理员可访问
            .anyRequest().authenticated()
        );
        // ... 其他配置
        return http.build();
    }
}
```

**curl 示例：**

```bash
# 写入缓存（TTL 60 秒）
curl -X PUT \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"value":"{\"name\":\"Alice\"}","ttlSeconds":60}' \
  http://localhost:8080/api/v1/cache/user:1001

# 读取缓存
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/cache/user:1001

# 查看统计
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/cache/stats

# 清理过期条目
curl -X POST -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/cache/cleanup
```

---

### 6.4 场景四：自定义持久化实现

如果需要接入 Redis、MongoDB 或其他存储后端，实现 `CachePersistence` 接口并注册为 Spring Bean 即可：

```java
@Component
public class RedisCachePersistence implements CachePersistence {

    private final RedisTemplate<String, CacheEntry> redisTemplate;

    public RedisCachePersistence(RedisTemplate<String, CacheEntry> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<CacheEntry> findByKey(String key) {
        CacheEntry entry = redisTemplate.opsForValue().get("cache:" + key);
        return Optional.ofNullable(entry);
    }

    @Override
    public CacheEntry save(CacheEntry entry) {
        Duration ttl = entry.getExpireAt() != null
            ? Duration.between(LocalDateTime.now(), entry.getExpireAt())
            : Duration.ofDays(365);
        redisTemplate.opsForValue().set("cache:" + entry.getKey(), entry, ttl);
        return entry;
    }

    // ... 实现其余方法
}
```

自动配置中的 `@ConditionalOnMissingBean(CachePersistence.class)` 确保：*
*一旦用户注册了自己的 `CachePersistence` Bean，框架不会再创建默认实现**，完全零冲突。

---

### 6.5 场景五：定时清理过期数据

JPA 模式下建议配置定时任务定期清理持久化层中的过期数据，防止数据库无限膨胀：

```java
@Component
public class CacheCleanupScheduler {

    private final IDistributedCache cache;

    public CacheCleanupScheduler(IDistributedCache cache) {
        this.cache = cache;
    }

    @Scheduled(fixedRate = 3600_000)  // 每小时执行一次
    public void cleanupExpiredEntries() {
        int deleted = cache.cleanupExpiredEntries();
        log.info("Cache cleanup: deleted {} expired entries", deleted);
    }
}
```

并在启动类上开启调度：

```java
@SpringBootApplication
@EnableScheduling
public class YourApplication { ... }
```

---

## 七、常见问题

**Q: 为什么 `get()` 读到过期数据后没有立即删除 DB 记录？**

A: `get()` 方法可能在只读事务上下文中被调用（如 `@Transactional(readOnly=true)` 的 Service 中），此时在
DB 执行删除操作会报错。框架采用"懒删除"策略，过期数据通过 `cleanupExpiredEntries()` 定期批量清理，避免破坏事务隔离性。

**Q: DB 挂掉后缓存还能用吗？**

A: 可以。`set()`/`delete()` 等写操作对 DB 的操作均被 `try-catch` 包裹，DB
失败时会输出 `[CACHE-DEGRADATION]` 警告并继续使用内存缓存服务读请求。新写入的数据仅存于内存，DB
恢复后无法自动同步，需根据业务决策是否重建。

**Q: 多实例部署时，一个实例修改了缓存，另一个实例的内存缓存会失效吗？**

A: **不会自动失效**。当前架构中 L1 内存缓存没有跨实例缓存失效通知机制。多实例环境下，只有 L2
数据库数据是共享的。L1 的数据依靠 TTL 过期后从 DB 重新加载。如需强一致性，建议缩短内存 TTL 或改用 Redis
等原生分布式缓存方案替换 L2。

**Q: 内存缓存 maxSize 满了怎么办？**

A: Caffeine 按 LRU 策略自动驱逐最久未访问的条目，被驱逐的数据仍在 L2 数据库中，下次访问时重新预热到
L1。

---

## 八、构建与测试

```bash
# 构建整个 cache 模块（含 core 和 starter）
mvn -pl cache -am clean install

# 仅运行 core 模块单元测试
mvn -pl cache/core test

# 仅运行 starter 模块单元测试
mvn -pl cache/starter test
mvn -pl cache/starter test
```

## 贡献

欢迎提交 PR 与 issue。建议：

- 为变更添加单元测试
- 在可能的情况下保持 API 向后兼容
