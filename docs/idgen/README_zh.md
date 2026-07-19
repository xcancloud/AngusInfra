# 分布式 ID 生成（angus-infra-idgen）

[English](README.md) | [中文](README_zh.md)

基于百度 [UidGenerator](https://github.com/baidu/uid-generator) 二次开发：提供 **UID**
（64-bit Snowflake 变体）与 **BID**（可读业务编码），支持 JPA、多库与 Spring Boot 自动装配。

> **不适合：** 需要连续无空洞的严格序号（号段会因重启丢弃未用完部分）；跨机房强中心化发号
> 且不能接受本地缓存语义的场景需单独评估。

---

## 目录

1. [概述](#1-概述)
2. [架构](#2-架构)
3. [数据模型](#3-数据模型)
4. [配置项](#4-配置项)
5. [接入指南](#5-接入指南)
6. [API 参考](#6-api-参考)
7. [实例 ID 分配](#7-实例-id-分配)
8. [性能提示](#8-性能提示)
9. [最佳实践](#9-最佳实践)
10. [排查指南](#10-排查指南)

---

## 1. 概述

| | **UidGenerator** | **BidGenerator** |
|--|------------------|------------------|
| 输出 | `long` | `String` |
| 用途 | DB PK、trace、高吞吐内部 ID | 订单号、合同号、工单号 |
| 状态 | 内存 RingBuffer + `angus_instance` | `angus_id_config` + 本地号段 / Redis |
| 默认实现 | `CachedUidGenerator` | `DefaultBidGenerator` |

| 组件 | 职责 |
|------|------|
| `UidGenerator` / `CachedUidGenerator` | UID 主 API（RingBuffer 缓存） |
| `BidGenerator` | BID 主 API |
| `InstanceIdAssigner` | workerId 分配 SPI |
| `ConfigIdAssigner` | BID 配置与 DB 号段 |
| `DistributedIncrAssigner` | Redis INCRBY SPI |
| `SnowflakeIdGenerator` | Hibernate PK 桥接 |

**设计原则**

1. **UID 位布局稳定** — 上线后勿改 `timeBits` / `workerBits` / `seqBits` / `epochStr`。
2. **Disposable workerId** — 同一 host:port **每次启动 id+1**（非复用）。
3. **BID 号段化** — DB/Redis 取段后本地 `AtomicLong` 消费；允许断号。
4. **租户模板** — `TENANT` 作用域可从 `tenantId=-1` 模板行自动克隆。

**UID 默认位布局（代码权威值）：**

```
sign(1) | deltaSeconds(32) | workerId(13) | sequence(18) = 64
epoch = 2021-01-01  → 约 136 年秒级寿命；worker ≈8192；seq/s ≈262144
```

---

## 2. 架构

### UID

```
启动 → DisposableInstanceIdAssigner.assignInstanceIdByParam(host,port,type)
     → CachedUidGenerator.afterPropertiesSet()
          → BitsAllocator + RingBuffer 预填充
     → SnowflakeIdGenerator.setUidGenerator(...)
调用 getUID() → RingBuffer.take()；失败则 fallback 同步 nextId()
```

### BID

```
getId(bizKey[, tenantId])
  → 缓存 IdConfig；miss 则查 DB（TENANT 可克隆模板）
  → Mode.DB: UPDATE max_id = max_id + step
    Mode.REDIS: Redis INCRBY(key, step)   // 仍有本地号段，非每次 +1
  → AtomicLong 消费 → Format 拼装
```

---

## 3. 数据模型

脚本：`classpath:schema/mysql/idgen-schema.sql`（Postgres 同目录）

### 3.1 `angus_instance`

| 列 | 说明 |
|----|------|
| `pk` | 主键 |
| `id` | workerId（每次启动 +1） |
| `host` / `port` | 实例标识；UNIQUE(`host`,`port`) |
| `instance_type` | HOST / CONTAINER 等 |
| `create_date` / `modified_date` | 时间戳 |

> schema 中 `id` **无 AUTO_INCREMENT**；新实例由代码写入 `id=1`。

### 3.2 `angus_id_config`

| 列 | 说明 |
|----|------|
| `biz_key` | 业务键 |
| `format` | `SEQ` / `PREFIX_SEQ` / `DATE_SEQ` / `PREFIX_DATE_SEQ` |
| `prefix` / `date_format` / `seq_length` | 格式参数 |
| `mode` | `DB` / `REDIS` |
| `scope` | `PLATFORM` / `TENANT` |
| `tenant_id` | 默认 `-1`（PLATFORM） |
| `max_id` / `step` | 号段游标与步长 |
| UNIQUE | (`biz_key`, `tenant_id`) |

种子示例：

```sql
INSERT INTO angus_id_config
  (pk, biz_key, format, prefix, date_format, seq_length,
   mode, scope, tenant_id, max_id, step, create_date, modified_date)
VALUES
  (UUID(), 'order', 'PREFIX_DATE_SEQ', 'ORD', 'YYYYMMDD', 10,
   'DB', 'PLATFORM', -1, 0, 5000, NOW(), NOW());
```

---

## 4. 配置项

前缀：`angus.idgen`  
**注意：** AutoConfiguration 的 `CoreCondition` 要求配置中**显式** `angus.idgen.enabled=true`
（缺省不装配，即便 Java 字段默认是 `true`）。

| 配置项 | 默认值（代码） | 说明 |
|--------|----------------|------|
| `enabled` | `true`（字段） | 需环境中显式为 true |
| `uid.timeBits` | `32` | 秒级时间位 |
| `uid.workerBits` | `13` | 实例/重启次数容量 |
| `uid.seqBits` | `18` | 每秒序列 |
| `uid.epochStr` | `2021-01-01` | `yyyy-MM-dd` |
| `uid.retriesNum` | `3` | 获取 instanceId 重试 |
| `cached.boostPower` | `2` | `bufferSize = (maxSeq+1) << boostPower` |
| `cached.paddingFactor` | `50` | **已声明但未接线**（硬编码 50） |
| `cached.scheduleInterval` | `300` | 定时填充秒数；`>0` 启用 |
| `cached.rejectionPolicy` | `BLOCK` | `BLOCK` / `DISCARD` / `EXCEPTION` |
| `bid.initialMapCapacity` | `512` | **唯一生效的 bid 配置字段** |
| `bid.maxStep` / `maxBatchNum` / `maxSeqLength` | 有默认 | Properties 有值，运行时用接口常量 |

相关非 idgen 配置：

```yaml
info:
  app:
    runtime: ${RUNTIME:HOST}   # 必填：InstanceInfoConfig

angus:
  idgen:
    enabled: true
    uid:
      timeBits: 32
      workerBits: 13
      seqBits: 18
      epochStr: "2021-01-01"
      retriesNum: 3
    cached:
      boostPower: 2
      scheduleInterval: 300
      rejectionPolicy: BLOCK
    bid:
      initialMapCapacity: 512
  datasource:
    extra:
      entityPackages:
        - cloud.xcan.angus.idgen.entity
    mysql:
      schema:
        - schema/mysql/idgen-schema.sql
```

Redis BID 模式需要可用的 `RedisTemplate<String, Object>` Bean。

---

## 5. 接入指南

### 5.1 依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.idgen-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

还需：DataSource + JPA、`dataSourceInitializer` Bean（`@DependsOn`）、
`info.app.runtime`、实体扫描 `cloud.xcan.angus.idgen.entity`、
仓库包 `cloud.xcan.angus.idgen.jpa`（推荐）。

### 5.2 自动装配

`IdGenAutoConfiguration`（经 `spring.factories`；条件：`enabled=true`）创建：

| Bean | 说明 |
|------|------|
| `cachedUidGenerator` | `CachedUidGenerator`（**不是** README 旧名 `uidGenerator`） |
| `instanceIdAssigner` | `DisposableInstanceIdAssigner` |
| `BidGenerator` | `DefaultBidGenerator` |
| `disposableConfigIdAssigner` | BID DB 配置/号段 |
| `DistributedIncrAssigner` | 可选 Redis |

### 5.3 业务调用

```java
@Resource
private CachedUidGenerator cachedUidGenerator; // 或 UidGenerator

@Resource
private BidGenerator bidGenerator;

long uid = cachedUidGenerator.getUID();
String orderNo = bidGenerator.getId("order");
String ticket = bidGenerator.getId("ticket", 1001L);
List<String> batch = bidGenerator.getIds("order", 100);
```

### 5.4 Hibernate 主键

实体使用 Angus 侧 `SnowflakeIdGenerator`（starter 启动时注入 `UidGenerator`）。

### 5.5 自定义 InstanceIdAssigner

```java
@Bean
public InstanceIdAssigner myInstanceIdAssigner() {
  return new InstanceIdAssigner() {
    public Long assignInstanceIdByEnv() { /* ZK / 固定配置 */ return 1L; }
    public Long assignInstanceIdByParam(String host, String port, InstanceType type) {
      return assignInstanceIdByEnv();
    }
  };
}
```

须保证 `0 <= workerId <= 2^workerBits - 1` 且多实例不冲突。

---

## 6. API 参考

### `UidGenerator`

| 方法 | 说明 |
|------|------|
| `getUID()` | 生成 long |
| `parseUID(long)` | JSON：datetime / instanceId / sequence |

### `BidGenerator`

| 方法 | 说明 |
|------|------|
| `getId(bizKey)` | PLATFORM |
| `getId(bizKey, tenantId)` | TENANT |
| `getIds(bizKey, batchNum[, tenantId])` | 批量；`batchNum ≤ MAX_BATCH_NUM(10000)` |

常量：`GLOBAL_TENANT_ID=-1`，`MAX_STEP=1000000`，`MAX_SQE_LENGTH=40`。

### 枚举

- `Format`：`SEQ`, `PREFIX_SEQ`, `DATE_SEQ`, `PREFIX_DATE_SEQ`
- `Mode`：`DB`, `REDIS`
- `Scope`：`PLATFORM`, `TENANT`
- `DateFormat`：`YYYY`, `YYYYMM`, `YYYYMMDD`

异常：`IdGenerateException`。

---

## 7. 实例 ID 分配

默认 `DisposableInstanceIdAssigner`：

1. host/port/type 来自 `ServerProperties` + `info.app.runtime`
2. 已存在 `(host,port)` → `UPDATE id = id + 1` → 返回新 id
3. 不存在 → `INSERT id=1`

即：**同一实例每次重启消耗一个新 workerId**。高频重启应加大 `workerBits`、减小 `seqBits`。

> 多机首次启动可能都拿到 `id=1`（新 host:port），存在 UID 冲突风险；生产应确保分配策略全局唯一，
> 或提供自定义 `InstanceIdAssigner`。

---

## 8. 性能提示

详见模块内 `performance/UIDPerformance(_zh).md`、`BIDPerformance(_zh).md`：

- CachedUidGenerator 稳态约 **百万级～六百万 QPS**（视机器而定）
- BID：`step` 建议 1000–10000；更大 step 提高吞吐但放大断号窗口
- Redis 模式仍是「取段 + 本地消费」，不是每次远程 +1

---

## 9. 最佳实践

1. **上线后禁止改位宽与 epoch**。
2. 高频重启加大 `workerBits`；长寿命加大 `timeBits`。
3. BID `step` 取 1000–10000；`MAX_BATCH_NUM` 应 ≤ step。
4. TENANT 业务先插入 `tenant_id=-1` 模板行。
5. **禁止手改 `max_id`**。
6. PLATFORM 不要传非 `-1` 的 tenantId。
7. 接受 BID 断号；需要严格连续序号请另建方案。
8. 配置前缀是 `angus.idgen`（不是注释里的 `xcan.idgen`）。

---

## 10. 排查指南

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| Bean 未创建 | `enabled` 未显式 true | 配置 `angus.idgen.enabled=true` |
| instanceId 获取失败 | DB/表不可用 | 建表、查重试与数据源 |
| 时钟回拨异常 | NTP 回拨 | 修时钟；避免回拨机器发号 |
| BID Redis 失败 | 无 RedisTemplate | 提供 Bean 或改 `Mode.DB` |
| 配置未找到 | 未插 `angus_id_config` | 补种子数据 |
| seqLength 溢出 | 序列位数超配置 | 加大 `seq_length` 或换 Format |
| UID 冲突疑虑 | 多机同为 workerId=1 | 自定义 Assigner / 检查分配 |
| `paddingFactor` 不生效 | 未接线 | 可忽略；硬编码 50 |

---

## 相关类

| 类 | 模块 |
|----|------|
| `cloud.xcan.angus.idgen.UidGenerator` | `idgen-core` |
| `cloud.xcan.angus.idgen.uid.CachedUidGenerator` | `idgen-core` |
| `cloud.xcan.angus.idgen.BidGenerator` | `idgen-core` |
| `cloud.xcan.angus.idgen.autoconfigure.IdGenAutoConfiguration` | `idgen-starter` |
| `cloud.xcan.angus.idgen.autoconfigure.IdGenProperties` | `idgen-starter` |
| `cloud.xcan.angus.persistence.jpa.identity.SnowflakeIdGenerator` | `jpa-core` |
