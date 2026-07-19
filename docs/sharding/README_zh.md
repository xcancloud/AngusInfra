# 分库分表（angus-infra-sharding）

[English](README.md) | [中文](README_zh.md)

轻量、生产可用的 Spring Boot + Spring Data JPA **分库分表**框架：通过 ThreadLocal
`ShardContext` 驱动数据源路由与 SQL 表名改写，支持按需建表与可插拔策略。

> **两种模式：**  
> 1. **全量分库分表** — `@Sharding` + 独立 `shardingEntityManagerFactory`  
> 2. **嵌入式仅分表** — `@ShardedRepository` + `@ShardedTable(shardKey=...)`（走主库，改表名）

---

## 目录

1. [概述](#1-概述)
2. [架构](#2-架构)
3. [数据模型](#3-数据模型)
4. [配置项](#4-配置项)
5. [接入指南](#5-接入指南)
6. [API 参考](#6-api-参考)
7. [路由与表名规则](#7-路由与表名规则)
8. [从 metricsds 迁移](#8-从-metricsds-迁移)
9. [最佳实践](#9-最佳实践)
10. [排查指南](#10-排查指南)

---

## 1. 概述

| 组件 | 职责 |
|------|------|
| `ShardingAspect` | 解析 `@Sharding`，写入 `ShardContext` |
| `ShardingRoutingDataSource` | 按 `dataSourceKey` 选库 |
| `ShardingTableInterceptor` | Hibernate `StatementInspector` 改写表名 |
| `ShardTableManager` | 按需执行 DDL 模板建表 |
| `ShardTableRegistry` | 跟踪已建物理表（内存 / JDBC / JPA） |
| `ShardingStrategy` | 计算 dbIndex / tableIndex |

**设计原则**

1. **ThreadContext 驱动** — 进入切面 set、退出 clear，避免泄漏。
2. **双层分片** — DB 路由 + SQL 表名改写。
3. **SPI 可扩展** — Strategy / KeyResolver / TableManager / Registry。
4. **按需建表** — classpath DDL 模板 + `CREATE TABLE IF NOT EXISTS`。
5. **默认关闭** — `angus.sharding.enabled=false`，显式开启。

---

## 2. 架构

```
Repository method
    │
    ├─ @Sharding ──────────────▶ ShardingAspect
    │                               ├─ ShardKeyResolver → shardKey / tableKey
    │                               ├─ ShardingStrategy → dbIndex / tableIndex
    │                               └─ ShardContext.set(ShardInfo)
    │
    └─ @ShardedRepository ─────▶ ShardedRepositoryAspect（需手工注册 Bean）
                                  └─ ShardContext.set(...)

JPA / JDBC
    → ShardingRoutingDataSource.determineCurrentLookupKey()
    → Hibernate SQL
    → ShardingTableInterceptor.inspect(sql)  // 改写表名 + ensureTablesExist
```

---

## 3. 数据模型

### 3.1 注册表 `angus_shard_table`（可选）

脚本：`classpath:schema/mysql/sharding-schema.sql`

| 列 | 类型 | 说明 |
|----|------|------|
| `table_name` | VARCHAR(255) | PK，物理表名 |
| `shard_key` | BIGINT | 分片键 |
| `db_index` | INT | 库索引 |
| `table_index` | BIGINT | 表索引；无二级分表时为 `-1` |

### 3.2 业务 DDL 模板（应用提供）

路径：`classpath:{schema-path}/{template-table-name}.sql`  
默认 `schema-path` 回落为 `sharding/schema`。

```sql
CREATE TABLE IF NOT EXISTS exec_sample (
  id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  payload TEXT,
  PRIMARY KEY (id)
);
CREATE INDEX idx_exec_sample_tenant ON exec_sample (tenant_id);
```

框架会替换表名；关闭二级索引时会去掉 `CREATE INDEX` 语句。

---

## 4. 配置项

前缀：`angus.sharding`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `enabled` | `false` | 总开关 |
| `shard-db-count` | `1` | 分库数（1–10） |
| `shard-table-count` | `1` | 每库分表数（1–50） |
| `enable-table-secondary-index` | `false` | 是否启用 `{table}-{shardKey}-{tableIndex}` |
| `db-type` | `mysql` | `mysql` / `postgres` |
| `username` / `password` | — | 分片库账号 |
| `entity-packages` | — | **启用时必填**，sharding EMF 扫描包 |
| `schema-path` | — | DDL 模板路径前缀（缺省 `sharding/schema`） |
| `template-table-names` | — | 模板表名列表 |
| `table-registry-enabled` | `false` | `true` 用 JDBC 持久化注册表 |
| `table-registry-table` | `angus_shard_table` | 注册表表名 |
| `mysql.urls` / `postgresql.urls` | — | 分片 JDBC URL 列表（数量 ≥ shard-db-count） |
| `aspect-pointcut` | — | **已废弃，无效果** |

Hikari 前缀：`angus.sharding.hikari`（`maximum-pool-size`、`minimum-idle` 等；≤0 表示不覆盖）。

```yaml
angus:
  sharding:
    enabled: true
    shard-db-count: 4
    shard-table-count: 8
    enable-table-secondary-index: false
    db-type: mysql
    username: root
    password: secret
    entity-packages:
      - com.example.myapp.domain.shard
    schema-path: sharding/schema
    template-table-names:
      - exec_sample
    mysql:
      urls:
        - jdbc:mysql://db-shard0:3306/myapp_s0
        - jdbc:mysql://db-shard1:3306/myapp_s1
        - jdbc:mysql://db-shard2:3306/myapp_s2
        - jdbc:mysql://db-shard3:3306/myapp_s3
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

---

## 5. 接入指南

### 5.1 依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.sharding-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

宿主还需：主 `DataSource` Bean、JDBC 驱动。

### 5.2 分片实体与注解

```java
@Entity
@Table(name = "exec_sample")
@ShardedTable
public class ExecSample {
  @Id
  private Long id;
  private Long tenantId;
  // ...
}
```

### 5.3 绑定独立 EMF / TM

```java
@EnableJpaRepositories(
    basePackages = "com.example.myapp.repo.shard",
    entityManagerFactoryRef = "shardingEntityManagerFactory",
    transactionManagerRef = "shardingTransactionManager"
)
```

### 5.4 方法级分片

```java
public interface ExecSampleRepo extends JpaRepository<ExecSample, Long> {

  @Sharding(shardKey = "tenantId")
  List<ExecSample> findByTenantId(@Param("tenantId") Long tenantId);
}
```

- `shardKey` 为空：取第一个 `Long` 参数。
- 需要 `javac -parameters` 或 `@Param`。
- 解析失败时默认 `shardKey=0`（静默落到 `shard0DataSource`）。

### 5.5 可选：JPA 注册表

主 EMF 扫描 `ShardTableEntity` + `ShardTableJpaRepository`，则自动使用
`JpaShardTableRegistry`（优先于 InMemory/Jdbc）。

### 5.6 嵌入式仅分表（可选）

```java
@ShardedRepository
public interface OrderRepo extends JpaRepository<Order, Long> { }

@Entity
@ShardedTable(shardKey = "tenantId", tableCount = 8)
public class Order { ... }
```

**必须手工注册切面：**

```java
@Bean
public ShardedRepositoryAspect shardedRepositoryAspect(
    ShardingStrategy strategy, List<ShardKeyResolver> resolvers) {
  return new ShardedRepositoryAspect(strategy, resolvers);
}
```

`dataSourceKey` 默认 `"shard"`（≠ `dataSource`），触发表名改写并 fallback 主库。

### 5.7 切换 Hash 策略

```java
@Bean
public ShardingStrategy shardingStrategy() {
  return new HashShardingStrategy();
}
```

默认是 `ModuloShardingStrategy`。

---

## 6. API 参考

### 注解

| 注解 | 目标 | 说明 |
|------|------|------|
| `@Sharding(shardKey, tableKey)` | Method | 全量分库分表 |
| `@ShardedTable(shardKey, tableCount)` | Type | 标记分片实体；`tableCount=0` 继承全局配置 |
| `@ShardedRepository(dataSourceKey, failOnUnresolved)` | Type | 嵌入式分表仓库 |

### `ShardContext` / `ShardInfo`

```java
ShardContext.set(new ShardInfo(shardKey, "shard0DataSource", tableIndex));
ShardContext.getDataSourceKey();
ShardContext.isSharded(); // !"dataSource".equals(key)
ShardContext.clear();
```

### SPI

| SPI | 关键方法 |
|-----|----------|
| `ShardingStrategy` | `computeDbIndex` / `computeTableIndex` |
| `ShardKeyResolver` | `resolve(...)`；`getOrder()` 越小越先 |
| `ShardTableManager` | `ensureTablesExist` / `isCreated` |
| `ShardTableRegistry` | `exists` / `save` / `findByShardKey` |

### 表名工具

```java
ShardTableNameUtils.buildName("exec_sample", 100);       // exec_sample-100
ShardTableNameUtils.buildName("exec_sample", 100, 3);    // exec_sample-100-3
```

---

## 7. 路由与表名规则

### 全量模式

1. `dbIndex = strategy.computeDbIndex(shardKey, shardDbCount)`
2. DS key = `shard{dbIndex}DataSource`
3. 开启二级索引：`tableIndex = computeTableIndex(tableKey, shardTableCount)`
4. 表名：`{table}-{shardKey}` 或 `{table}-{shardKey}-{tableIndex}`

### 嵌入式模式

1. 从实体字段 / 参数取分片值 → `tableIndex`
2. `ShardInfo.shardKey` 槽位存的是 **tableIndex**（语义不同于全量模式）
3. 表名：`{table}-{tableIndex}`；路由 miss 时回落主库

### SQL 改写限制

仅处理 SELECT / INSERT / UPDATE / DELETE；复杂多表 JOIN 通常只改第一个匹配表。

---

## 8. 从 metricsds 迁移

| 旧 | 新 |
|----|-----|
| `@ShardingTable` | `@ShardedTable` |
| `xcan.datasource.metrics.*` | `angus.sharding.*` |
| metrics EMF Bean 名 | `shardingEntityManagerFactory` / `shardingTransactionManager` |

---

## 9. 最佳实践

1. 分片仓库必须绑定 sharding EMF/TM，注册表仓库必须在主 EMF。
2. DDL 始终使用 `CREATE TABLE IF NOT EXISTS`。
3. 用 `@Param` 明确分片参数，避免静默落到 shard0。
4. `saveAll` 不要混多个分片键。
5. 嵌入式模式务必注册 `ShardedRepositoryAspect`，并保持 `failOnUnresolved=true`。
6. 上线后不要随意改 `shard-db-count` / 策略算法，否则数据路由漂移。

---

## 10. 排查指南

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 整套未加载 | `enabled=false` | 设为 `true` |
| EMF 启动失败 | 未配 `entity-packages` | 补全包路径 |
| 总打到主库 | 仓库绑错 EMF | 检查 `@EnableJpaRepositories` |
| 总打到 shard0 | 参数名解析失败 | 开 `-parameters` 或加 `@Param` |
| 表名未改写 | 未设 ShardContext / 表未标注 | 查注解与 `isSharded()` |
| DDL 反复失败 | 模板缺失或语法错 | 查 `schema-path` 与 SQL |
| 嵌入式不生效 | 未注册 Aspect | 声明 `ShardedRepositoryAspect` Bean |

---

## 相关类

| 类 | 模块 |
|----|------|
| `cloud.xcan.angus.sharding.ShardingAutoConfiguration` | `sharding-starter` |
| `cloud.xcan.angus.sharding.ShardingAspect` | `sharding-starter` |
| `cloud.xcan.angus.sharding.ShardingRoutingDataSource` | `sharding-starter` |
| `cloud.xcan.angus.sharding.annotation.Sharding` | `sharding-core` |
| `cloud.xcan.angus.sharding.context.ShardContext` | `sharding-core` |
| `cloud.xcan.angus.sharding.strategy.ShardingStrategy` | `sharding-core` |
