# AngusInfra Sharding 分库分表框架

[English](README.md) | [中文](README_zh.md)

[![Maven Central](https://img.shields.io/maven-central/v/cloud.xcan.angus/xcan-angusinfra.sharding-starter)](https://central.sonatype.com/artifact/cloud.xcan.angus/xcan-angusinfra.sharding-starter)
[![License](https://img.shields.io/badge/license-GPLv3-blue)](https://www.gnu.org/licenses/gpl-3.0.html)

一个轻量、生产就绪的 **Spring Boot + Spring Data JPA 分库分表框架**。
从 `metricsds` 模块迁移并增强，提供多租户分库路由、SQL 动态表名改写、自动建表及可扩展 SPI。

---

## 模块结构

```
sharding/
├── core/      纯 Java 抽象层 —— 注解、上下文、策略 SPI、表注册 SPI
└── starter/   Spring Boot 自动配置 —— AOP 切面、路由 DataSource、JPA 集成
```

### `sharding-core`

| 包 | 说明 |
|---|---|
| `annotation` | `@Sharding`（方法）、`@ShardedTable`（实体） |
| `config` | `ShardingProperties`、`HikariShardingProperties` |
| `context` | `ShardContext`（线程局部持有器）、`ShardInfo` |
| `resolver` | `ShardKeyResolver` SPI |
| `strategy` | `ShardingStrategy` SPI、`ModuloShardingStrategy`、`HashShardingStrategy` |
| `table` | `ShardTableManager` SPI、`ShardTableRegistry` SPI、`ShardTableNameUtils`、`SqlTableMatcher` |

### `sharding-starter`

| 包 | 说明 |
|---|---|
| `autoconfigure` | `ShardingAutoConfiguration`、`ShardingAspect`、`ShardingTableInterceptor`、`ShardingRoutingDataSource`、`SqlTemplateTableManager` |
| `autoconfigure/registry` | `InMemoryShardTableRegistry`（默认内存实现）、`JdbcShardTableRegistry`（JDBC 持久化） |
| `autoconfigure/resolver` | `DefaultShardKeyResolver`（默认分片键解析器） |
| `autoconfigure/jpa` | `ShardTableEntity`、`ShardTableJpaRepository`、`JpaShardTableRegistry`、`ShardingJpaAutoConfiguration` |

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.sharding-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

### 2. 配置属性

```yaml
angus:
  sharding:
    enabled: true
    shard-db-count: 4            # 分库数量（1-10）
    shard-table-count: 8         # 每库分表数量（1-50）
    enable-table-secondary-index: false
    db-type: mysql               # mysql | postgres
    username: root
    password: secret
    entity-packages:
      - com.example.myapp.domain.shard
    schema-path: sharding/schema # DDL 模板类路径前缀
    template-table-names:
      - exec_sample
      - node_usage
    # 可选：启用 JDBC 持久化注册表
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

### 3. 标注实体

```java
@Entity
@Table(name = "exec_sample")
@ShardedTable                    // 开启该表的 SQL 表名改写
public class ExecSample {
  @Id
  private Long id;
  private Long tenantId;
  // ...
}
```

### 4. 配置 JPA 仓库

为分片实体单独配置 `@EnableJpaRepositories`，指向框架提供的 EntityManagerFactory 和事务管理器：

```java
@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "shardingEntityManagerFactory",
    transactionManagerRef   = "shardingTransactionManager",
    basePackages            = "com.example.myapp.infra.shard"
)
public class ShardingJpaConfig { }
```

### 5. 标注仓库方法

```java
public interface ExecSampleRepo extends JpaRepository<ExecSample, Long> {

  @Sharding(shardKey = "tenantId")
  List<ExecSample> findByTenantId(Long tenantId);

  @Sharding(shardKey = "tenantId", tableKey = "execId")
  Optional<ExecSample> findByTenantIdAndExecId(Long tenantId, Long execId);
}
```

框架在每次调用时自动：
1. 通过解析器链提取分片键
2. 设置当前线程 `ShardContext`
3. 路由到正确的分库 DataSource
4. 在 SQL 中改写表名

---

## 核心原理

### 分片策略

| 策略 | 描述 |
|---|---|
| `ModuloShardingStrategy`（默认） | 取模算法：`|shardKey| % shardDbCount` |
| `HashShardingStrategy` | MurmurHash3 混淆，对连续 ID（如 Snowflake）分布更均匀 |

注册自定义策略：

```java
@Bean
public ShardingStrategy myStrategy() {
  return new MyHashRingStrategy();
}
```

### 分片键解析链

`ShardKeyResolver` SPI 控制如何从方法参数中提取分片键。内置 `DefaultShardKeyResolver` 支持：

1. **命名字段反射**（`@Sharding(shardKey="tenantId")`）：在方法参数对象或 `Iterable` 首元素中查找
2. **方法参数名匹配**（需要 `javac -parameters` 编译参数）
3. **首个 `Long` 参数兜底**

注册更高优先级的自定义解析器（`getOrder()` 值更小）：

```java
@Bean
public ShardKeyResolver securityContextResolver() {
  return new ShardKeyResolver() {
    @Override
    public Long resolve(Object[] args, String[] paramNames,
        Sharding sharding, String fieldName) {
      return SecurityContextHolder.getTenantId(); // 从安全上下文读取
    }
    @Override
    public int getOrder() { return 10; } // 优先于默认的 100
  };
}
```

### SQL 表名改写

`ShardingTableInterceptor` 实现 Hibernate `StatementInspector`，在每条 SQL 执行前：

1. 识别表名是否属于 `@ShardedTable` 注册集合
2. 将表名替换为分片格式：

```
exec_sample  →  exec_sample-{shardKey}          （无二级分表）
exec_sample  →  exec_sample-{shardKey}-{idx}    （开启二级分表）
```

### 分片表注册表

| 实现 | 持久化 | 适用场景 |
|---|---|---|
| `InMemoryShardTableRegistry` | JVM 生命周期 | 默认；DDL 使用 `CREATE TABLE IF NOT EXISTS` 时安全 |
| `JdbcShardTableRegistry` | 主库 JDBC | 设置 `angus.sharding.table-registry-enabled=true` |
| `JpaShardTableRegistry` | 主库 JPA | 在主库注册 `ShardTableJpaRepository` Bean |

#### 使用 JPA 注册表

```java
@SpringBootApplication
@EntityScan(basePackageClasses = {ShardTableEntity.class, MyEntity.class})
@EnableJpaRepositories(basePackageClasses = {ShardTableJpaRepository.class, MyRepo.class})
public class MyApplication { ... }
```

当 Spring 上下文存在 `ShardTableJpaRepository` Bean 时，`ShardingJpaAutoConfiguration` 自动将
`JpaShardTableRegistry` 注册为活跃的 `ShardTableRegistry`。

---

## DDL 模板

在类路径的 `<schema-path>/<table-name>.sql` 放置 DDL 模板，如：

```
src/main/resources/sharding/schema/exec_sample.sql
```

示例内容：

```sql
CREATE TABLE IF NOT EXISTS exec_sample (
  id         BIGINT      NOT NULL,
  tenant_id  BIGINT      NOT NULL,
  payload    TEXT,
  PRIMARY KEY (id)
);
CREATE INDEX idx_exec_sample_tenant ON exec_sample (tenant_id);
```

框架将模板中所有 `exec_sample` 替换为分片表名再执行 DDL。  
当 `enableTableSecondaryIndex=false` 时，`CREATE INDEX` 语句会自动去除。

---

## 配置参数参考

| 参数 | 默认值 | 说明 |
|---|---|---|
| `angus.sharding.enabled` | `false` | 是否启用分库分表 |
| `angus.sharding.shard-db-count` | `1` | 分库数量（1–10） |
| `angus.sharding.shard-table-count` | `1` | 每库分表数量（1–50） |
| `angus.sharding.enable-table-secondary-index` | `false` | 是否开启二级分表 |
| `angus.sharding.db-type` | `mysql` | `mysql` 或 `postgres` |
| `angus.sharding.username` | | 所有分库共享用户名 |
| `angus.sharding.password` | | 所有分库共享密码 |
| `angus.sharding.entity-packages` | | JPA 实体扫描包 |
| `angus.sharding.schema-path` | | DDL 模板类路径前缀 |
| `angus.sharding.template-table-names` | | 动态建表的模板表名列表 |
| `angus.sharding.table-registry-enabled` | `false` | 启用 JDBC 持久化注册表 |
| `angus.sharding.table-registry-table` | `angus_shard_table` | 注册表名 |
| `angus.sharding.mysql.urls` | | MySQL 各分库 JDBC URL |
| `angus.sharding.postgresql.urls` | | PostgreSQL 各分库 JDBC URL |
| `angus.sharding.hikari.*` | | HikariCP 连接池参数 |

---

## 从 `metricsds` 迁移

| 旧代码（`metricsds`） | 新代码（`sharding`） |
|---|---|
| `@Sharding(tableField="tenantId")` | `@Sharding(shardKey="tenantId")` |
| `@ShardingTable` | `@ShardedTable` |
| `xcan.datasource.metrics.*` | `angus.sharding.*` |
| `MetricsDataSourceContextHolder` | `ShardContext` |
| `MetricsDynamicDataSourceRouter` | `ShardingRoutingDataSource` |
| `MetricsDynamicDataSourceAspect` | `ShardingAspect` + `DefaultShardKeyResolver` |
| `TableSchemaManager` | `SqlTemplateTableManager` + `ShardTableRegistry` |

---

## 许可证

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html) © XCan Cloud
