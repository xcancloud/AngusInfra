# AngusInfra Sharding

[English](README.md) | [中文](README_zh.md)

[![Maven Central](https://img.shields.io/maven-central/v/cloud.xcan.angus/xcan-angusinfra.sharding-starter)](https://central.sonatype.com/artifact/cloud.xcan.angus/xcan-angusinfra.sharding-starter)
[![License](https://img.shields.io/badge/license-GPLv3-blue)](https://www.gnu.org/licenses/gpl-3.0.html)

A lightweight, production-ready **database sharding framework** for Spring Boot + Spring Data JPA.
Migrated and enhanced from the `metricsds` module, this library provides multi-tenant DB sharding,
dynamic table-name rewriting at SQL level, automatic schema creation, and an extensible SPI model.

---

## Module Structure

```
sharding/
├── core/          Pure Java abstractions – annotations, context, strategy SPI, table registry SPI
└── starter/       Spring Boot Auto-Configuration – AOP aspect, routing DataSource, JPA wiring
```

### `sharding-core`

| Package | Description |
|---|---|
| `annotation` | `@Sharding` (method), `@ShardedTable` (entity) |
| `config` | `ShardingProperties`, `HikariShardingProperties` |
| `context` | `ShardContext` (thread-local holder), `ShardInfo` |
| `resolver` | `ShardKeyResolver` SPI |
| `strategy` | `ShardingStrategy` SPI, `ModuloShardingStrategy`, `HashShardingStrategy` |
| `table` | `ShardTableManager` SPI, `ShardTableRegistry` SPI, `ShardTableNameUtils`, `SqlTableMatcher` |

### `sharding-starter`

| Package | Description |
|---|---|
| `autoconfigure` | `ShardingAutoConfiguration`, `ShardingAspect`, `ShardingTableInterceptor`, `ShardingRoutingDataSource`, `SqlTemplateTableManager` |
| `autoconfigure/registry` | `InMemoryShardTableRegistry`, `JdbcShardTableRegistry` |
| `autoconfigure/resolver` | `DefaultShardKeyResolver` |
| `autoconfigure/jpa` | `ShardTableEntity`, `ShardTableJpaRepository`, `JpaShardTableRegistry`, `ShardingJpaAutoConfiguration` |

---

## Quick Start

### 1. Add Dependency

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.sharding-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

### 2. Configure Properties

```yaml
angus:
  sharding:
    enabled: true
    shard-db-count: 4            # Number of DB shards (1-10)
    shard-table-count: 8         # Number of table shards per DB (1-50)
    enable-table-secondary-index: false
    db-type: mysql               # mysql | postgres
    username: root
    password: secret
    entity-packages:
      - com.example.myapp.domain.shard
    schema-path: sharding/schema # Classpath path to DDL templates
    template-table-names:
      - exec_sample
      - node_usage
    # Optional durable registry (JDBC-backed)
    table-registry-enabled: false
    table-registry-table: angus_shard_table
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

### 3. Annotate Entities

```java
@Entity
@Table(name = "exec_sample")
@ShardedTable                    // SQL rewriting activated for this table
public class ExecSample {
  @Id
  private Long id;
  private Long tenantId;
  // ...
}
```

### 4. Enable JPA Repositories for Sharded Entities

Add `@EnableJpaRepositories` pointing to the sharding entity manager factory and transaction manager:

```java
@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "shardingEntityManagerFactory",
    transactionManagerRef   = "shardingTransactionManager",
    basePackages            = "com.example.myapp.infra.shard"
)
public class ShardingJpaConfig { }
```

### 5. Annotate Repository Methods

```java
public interface ExecSampleRepo extends JpaRepository<ExecSample, Long> {

  @Sharding(shardKey = "tenantId")
  List<ExecSample> findByTenantId(Long tenantId);

  @Sharding(shardKey = "tenantId", tableKey = "execId")
  Optional<ExecSample> findByTenantIdAndExecId(Long tenantId, Long execId);
}
```

The framework intercepts each call, extracts the shard key via the resolver chain, sets the
`ShardContext`, routes the call to the correct shard DataSource, and rewrites table names in SQL.

---

## Key Concepts

### Sharding Strategy

The default `ModuloShardingStrategy` computes `|shardKey| % shardDbCount` for the DB index and
`|tableKey| % shardTableCount` for the secondary table index.

An alternative `HashShardingStrategy` uses MurmurHash3 mixing for better distribution of
sequential IDs (e.g. Snowflake).

Register a custom strategy by declaring a `ShardingStrategy` bean:

```java
@Bean
public ShardingStrategy myCustomStrategy() {
  return new MyHashRingStrategy();
}
```

### Shard Key Resolution

The `ShardKeyResolver` SPI controls how the numeric shard key is extracted from method arguments.
The built-in `DefaultShardKeyResolver` handles:

1. Named field (`@Sharding(shardKey="tenantId")`) on plain objects or first `Iterable` element
2. Method parameter name matching (requires `javac -parameters`)
3. First `Long` argument as a last-resort fallback

Register additional resolvers as Spring beans with a lower `getOrder()` value to take precedence:

```java
@Bean
public ShardKeyResolver tenantContextResolver() {
  return (args, paramNames, sharding, fieldName) -> SecurityContextHolder.getTenantId();
  // order defaults to 100; override getOrder() to raise priority
}
```

### Table Name Rewriting

`ShardingTableInterceptor` implements Hibernate's `StatementInspector`. For each SQL statement
it checks if the table name belongs to a registered `@ShardedTable` entity and rewrites it to the
sharded form:

```
exec_sample  →  exec_sample-{shardKey}          (no secondary index)
exec_sample  →  exec_sample-{shardKey}-{idx}    (with secondary index)
```

### Table Registry

| Implementation | Persistence | When to use |
|---|---|---|
| `InMemoryShardTableRegistry` | JVM lifetime | Default; safe with `CREATE TABLE IF NOT EXISTS` DDL |
| `JdbcShardTableRegistry` | Primary datasource (JDBC) | `angus.sharding.table-registry-enabled=true` |
| `JpaShardTableRegistry` | Primary datasource (JPA) | Provide `ShardTableJpaRepository` bean |

#### Using JPA Registry

```java
// 1. Include ShardTableEntity in your primary entity scan
@SpringBootApplication
@EntityScan(basePackageClasses = {ShardTableEntity.class, MyEntity.class})
@EnableJpaRepositories(basePackageClasses = {ShardTableJpaRepository.class, MyRepo.class})
public class MyApplication { ... }
```

The `ShardingJpaAutoConfiguration` detects `ShardTableJpaRepository` and automatically registers
`JpaShardTableRegistry` as the active `ShardTableRegistry`.

---

## DDL Templates

Place a SQL file per template table on the classpath at `<schema-path>/<table-name>.sql`:

```
src/main/resources/sharding/schema/exec_sample.sql
```

Example:

```sql
CREATE TABLE IF NOT EXISTS exec_sample (
  id         BIGINT      NOT NULL,
  tenant_id  BIGINT      NOT NULL,
  payload    TEXT,
  PRIMARY KEY (id)
);
CREATE INDEX idx_exec_sample_tenant ON exec_sample (tenant_id);
```

The manager replaces all occurrences of `exec_sample` with the sharded table name before
executing the DDL. When `enableTableSecondaryIndex=false`, `CREATE INDEX` statements are removed.

---

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `angus.sharding.enabled` | `false` | Master switch |
| `angus.sharding.shard-db-count` | `1` | Number of DB shards (1–10) |
| `angus.sharding.shard-table-count` | `1` | Number of table shards per DB (1–50) |
| `angus.sharding.enable-table-secondary-index` | `false` | Enable `…-{shardKey}-{idx}` table naming |
| `angus.sharding.db-type` | `mysql` | `mysql` or `postgres` |
| `angus.sharding.username` | | Shared username for all shard DBs |
| `angus.sharding.password` | | Shared password for all shard DBs |
| `angus.sharding.entity-packages` | | Packages to scan for JPA entities |
| `angus.sharding.schema-path` | | Classpath base path for DDL templates |
| `angus.sharding.template-table-names` | | Template table names for dynamic creation |
| `angus.sharding.table-registry-enabled` | `false` | Enable JDBC-backed shard table registry |
| `angus.sharding.table-registry-table` | `angus_shard_table` | Registry table name |
| `angus.sharding.mysql.urls` | | JDBC URLs for each MySQL shard |
| `angus.sharding.postgresql.urls` | | JDBC URLs for each PostgreSQL shard |
| `angus.sharding.hikari.*` | | HikariCP pool settings |

---

## Migration from `metricsds`

| Old (`metricsds`) | New (`sharding`) |
|---|---|
| `@Sharding(tableField="tenantId")` | `@Sharding(shardKey="tenantId")` |
| `@ShardingTable` | `@ShardedTable` |
| `xcan.datasource.metrics.*` | `angus.sharding.*` |
| `MetricsDataSourceContextHolder` | `ShardContext` |
| `MetricsDynamicDataSourceRouter` | `ShardingRoutingDataSource` |
| `MetricsDynamicDataSourceAspect` | `ShardingAspect` + `DefaultShardKeyResolver` |
| `TableSchemaManager` | `SqlTemplateTableManager` + `ShardTableRegistry` |

---

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html) © XCan Cloud
