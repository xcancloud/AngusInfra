ID Generator — idgen 模块
==========================

> **说明**：AngusInfra idgen 是在百度 [UidGenerator](https://github.com/baidu/uid-generator) 基础上
> 二次开发形成的分布式 ID 生成组件，并扩展了业务编码生成能力（BidGenerator）。
> 相较原版，主要变更：① 将 MyBatis 改为 JPA；② 将 Spring XML 改为 Spring Boot 自动装配；
> ③ 去除对 MySQL 的强依赖，支持 PostgreSQL / Oracle / SQL Server；
> ④ 新增可读性强的业务 ID 生成器 BidGenerator。

---

## 目录

1. [功能概述](#1-功能概述)
2. [架构设计](#2-架构设计)
3. [核心组件](#3-核心组件)
4. [数据库表设计](#4-数据库表设计)
5. [配置参考](#5-配置参考)
6. [三方接入说明](#6-三方接入说明)
7. [性能参考](#7-性能参考)
8. [注意事项](#8-注意事项)

---

## 1. 功能概述

idgen 模块提供两类 ID 生成器：

| 生成器              | 类型                 | 适用场景                    |
|------------------|--------------------|-------------------------|
| **UidGenerator** | 64位长整型，类 Snowflake | 数据库主键、分布式追踪 ID、高并发内部 ID |
| **BidGenerator** | 可读字符串，格式可配         | 订单号、合同编号、工单号等对外展示的业务编码  |

两者均以 Spring Boot Starter 方式集成，通过 `angus.idgen.enabled=true` 一键开启。

---

## 2. 架构设计

### 2.1 UidGenerator（Snowflake 变体）

```
 63      62...(62-timeBits+1)   ...(workerBits)   ...(seqBits)   0
  ┌─────┬──────────────────────┬─────────────────┬───────────────┐
  │sign │    deltaSeconds      │   workerId      │   sequence    │
  │ 1b  │      28b(默认)       │    22b(默认)    │   13b(默认)   │
  └─────┴──────────────────────┴─────────────────┴───────────────┘
```

- **sign（1 bit）**：固定为 0，保证生成 ID 为正数。
- **deltaSeconds（28 bit 默认）**：与 epoch（2016-05-20）的秒级差值，可表示约 8.7 年。
- **workerId（22 bit 默认）**：实例 ID，由 `angus_instance` 表的自增主键分配，最多支持 4M 实例。
- **sequence（13 bit 默认）**：同一秒内的序列号，每秒最多生成 8192 个 ID。

> 各段位数可通过配置自定义，三段之和必须为 63。

**DefaultUidGenerator**：同步生成，无额外内存开销，适合低并发或需要严格时序的场景。

**CachedUidGenerator**：在 DefaultUidGenerator 基础上增加 RingBuffer 预生成机制：

- 启动时预填充满 RingBuffer（默认容量 `(maxSequence+1) << boostPower`，约 32768 槽位）。
- 消费线程从 RingBuffer 头部取 UID，生产线程在后台异步补充。
- 当剩余槽位低于 `paddingFactor%`（默认 50%）时触发异步填充。
- 同时支持定时调度补充（默认每 5 分钟），防止长时间空闲后耗尽。
- RingBuffer 耗尽时降级为同步生成（`super.nextId()`），不抛异常。
- 使用 `PaddedAtomicLong`（Cache Line 对齐）消除伪共享，实测单机 QPS 可达 **600 万**。

### 2.2 BidGenerator（业务 ID）

```
┌──────────────────────┐     ┌──────────────────────────┐
│  业务调用方            │────▶│  DefaultBidGenerator     │
│  getId(bizKey)       │     │  ConcurrentHashMap(缓存)  │
└──────────────────────┘     └──────────┬───────────────┘
                                         │ 缓存不命中或段耗尽
                    ┌────────────────────┴────────────────────┐
                    │                                          │
              ┌─────▼──────┐                        ┌─────────▼──────┐
              │ DB 模式     │                        │ Redis 模式      │
              │ 从 id_config│                        │ INCRBY atomicly │
              │ 取号段并缓存 │                        │ 无需号段缓存     │
              └─────────────┘                        └─────────────────┘
```

- **DB 模式**：从 `angus_id_config` 表读取 `maxId` + `step`，在内存中维护 `AtomicLong`
  计数器，用完后再次取段。双重检查锁（DCL）保证并发安全。
- **Redis 模式**：通过 `INCRBY` 原子递增，无本地计数器，适合多实例强一致要求场景。
- **PLATFORM 范围**：所有租户共享一个号码空间（tenantId = -1）。
- **TENANT 范围**：每个租户独立号码空间，第一次调用时自动从 `tenantId=-1` 的模板克隆配置。

---

## 3. 核心组件

### 3.1 UidGenerator 体系

| 类/接口                           | 说明                                                                                    |
|--------------------------------|---------------------------------------------------------------------------------------|
| `UidGenerator`                 | 接口，定义 `getUID() → long`、`parseUID(long) → String`                                     |
| `BitsAllocator`                | 位操作工具，封装各段的移位与掩码运算                                                                    |
| `DefaultUidGenerator`          | 同步 Snowflake 实现，`nextId()` 加锁，处理时钟回拨与序列溢出                                             |
| `CachedUidGenerator`           | 扩展 Default，增加 RingBuffer + BufferPaddingExecutor                                      |
| `RingBuffer`                   | 基于数组的无锁环形缓冲区，`tail`/`cursor` 均为 `PaddedAtomicLong`                                    |
| `BufferPaddingExecutor`        | 负责异步填充 RingBuffer，包含线程池调用和定时调度两种触发方式                                                  |
| `InstanceIdAssigner`           | SPI，提供 `assignInstanceIdByEnv()` 和 `assignInstanceIdByParam()`                        |
| `DisposableInstanceIdAssigner` | 默认实现，读取环境变量 `HOST`/`HTTP_PORT`/`RUNTIME_ENV`，写入 `angus_instance` 表获取自增 ID 作为 workerId |

### 3.2 BidGenerator 体系

| 类/接口                      | 说明                                    |
|---------------------------|---------------------------------------|
| `BidGenerator`            | 接口，定义 `getId` / `getIds` 系列方法，含租户重载   |
| `AbstractBidGenerator`    | 抽象基类，实现格式拼装（PREFIX、DATE、SEQ 组合）       |
| `DefaultBidGenerator`     | 默认实现，ConcurrentHashMap 内存缓存 + DCL 初始化 |
| `ConfigIdAssigner`        | SPI，封装 `angus_id_config` 表的读写及号段分配    |
| `DistributedIncrAssigner` | SPI，封装 Redis `INCRBY` 原子递增            |

### 3.3 枚举类型

#### Format（编码格式）

| 值                 | 输出示例                  | 说明            |
|-------------------|-----------------------|---------------|
| `SEQ`             | `00000001`            | 纯序列号          |
| `PREFIX_SEQ`      | `ORD00000001`         | 前缀 + 序列号      |
| `DATE_SEQ`        | `2024090100000001`    | 日期 + 序列号      |
| `PREFIX_DATE_SEQ` | `ORD2024090100000001` | 前缀 + 日期 + 序列号 |

#### Mode（生成模式）

| 值       | 说明                                                     |
|---------|--------------------------------------------------------|
| `DB`    | 号段从数据库 `id_config.max_id` + `step` 获取，本地 AtomicLong 消费 |
| `REDIS` | 每次通过 Redis `INCRBY` 原子递增，无本地缓存断号风险                     |

#### Scope（唯一性范围）

| 值          | `tenantId` 行为                            |
|------------|------------------------------------------|
| `PLATFORM` | 所有调用使用 tenantId=-1 的同一配置行，平台全局唯一         |
| `TENANT`   | 首次调用时以模板行（tenantId=-1）克隆出对应 tenantId 的新行 |

#### DateFormat（日期格式）

| 值          | 示例         |
|------------|------------|
| `YYYY`     | `2024`     |
| `YYYYMM`   | `202409`   |
| `YYYYMMDD` | `20240901` |

---

## 4. 数据库表设计

### 4.1 angus_instance 表（workerId 分配）

```sql
CREATE TABLE `angus_instance` (
  `pk`            varchar(40)  NOT NULL COMMENT '主键（UUID）',
  `id`            bigint(21)   NOT NULL AUTO_INCREMENT COMMENT '自增workerId，用作Snowflake workerId段',
  `host`          varchar(160) NOT NULL DEFAULT '' COMMENT '实例IP/主机名',
  `port`          varchar(40)  NOT NULL DEFAULT '' COMMENT '实例HTTP端口',
  `instance_type` varchar(40)  NOT NULL DEFAULT '' COMMENT '实例类型（取自RUNTIME_ENV环境变量）',
  `create_date`   datetime     NOT NULL COMMENT '注册时间',
  `modified_date` datetime     NOT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`pk`),
  UNIQUE KEY `uidx_host_port` (`host`, `port`) USING BTREE
) ENGINE=InnoDB COMMENT='UID生成器实例注册表';
```

**字段说明**：

- `id`：自增长整型，即分配给实例的 `workerId`，需确保不超过 `2^workerBits - 1`。
- `host` + `port`：联合唯一约束，保证同一实例重启时复用原 workerId。
- 环境变量映射：`HOST` → host，`HTTP_PORT` → port，`RUNTIME_ENV` → instance_type。

### 4.2 angus_id_config 表（业务 ID 配置）

```sql
CREATE TABLE `angus_id_config` (
  `pk`           varchar(40)  NOT NULL COMMENT '主键（UUID）',
  `biz_key`      varchar(80)  NOT NULL COMMENT '业务标识，如 order、contract',
  `format`       varchar(16)  NOT NULL COMMENT '编码格式：SEQ/PREFIX_SEQ/DATE_SEQ/PREFIX_DATE_SEQ',
  `prefix`       varchar(4)   NOT NULL DEFAULT '' COMMENT '编码前缀（1-4字符）',
  `date_format`  varchar(8)   NOT NULL DEFAULT '' COMMENT '日期格式：YYYY/YYYYMM/YYYYMMDD',
  `seq_length`   int(11)      NOT NULL DEFAULT 8 COMMENT '序号位数，<=0时变长自增',
  `mode`         varchar(8)   NOT NULL COMMENT '生成模式：DB/REDIS',
  `scope`        varchar(16)  NOT NULL COMMENT '唯一性范围：PLATFORM/TENANT',
  `tenant_id`    bigint(20)   NOT NULL DEFAULT -1 COMMENT '租户ID，-1表示平台级',
  `max_id`       bigint(20)   NOT NULL DEFAULT 0 COMMENT 'DB模式当前已分配最大值',
  `step`         bigint(20)   NOT NULL COMMENT '号段步长，建议1000-10000',
  `create_date`  datetime     NOT NULL COMMENT '创建时间',
  `modified_date` datetime    NOT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`pk`),
  UNIQUE INDEX `uidx_biz_key_tenant_id` (`biz_key`, `tenant_id`)
) ENGINE=InnoDB COMMENT='业务ID配置表';
```

**关键约束**：

- `(biz_key, tenant_id)` 联合唯一，TENANT 范围下每个租户独占一行。
- `max_id`：DB 模式下记录已批量分配的上界，每次取段后以 `UPDATE ... SET max_id = max_id + step` 推进。
- `step` 建议范围 1000–10000；过小频繁 IO，过大重启后浪费号段（产生断号）。

---

## 5. 配置参考

```yaml
angus:
  idgen:
    enabled: true                   # 主开关，true 时激活全部 Bean

    # ── UidGenerator 位分配 ──────────────────────────────────────────
    uid:
      timeBits: 28                  # 时间位数（秒级），与 epochStr 共同决定可用年限
      workerBits: 22                # WorkerId 位数，最大实例数 = 2^22 ≈ 400万
      seqBits: 13                   # 每秒序列号位数，最大 QPS = 2^13/s ≈ 8192
      epochStr: "2016-05-20"        # 起始基准日期，建议设为项目上线日期
      retriesNum: 3                 # 时钟回拨时的重试次数

    # ── CachedUidGenerator RingBuffer 调优 ───────────────────────────
    cached:
      boostPower: 2                 # RingBuffer 容量倍数：bufferSize = (maxSeq+1) << boostPower
      paddingFactor: 50             # 剩余量低于此百分比时触发异步填充（0-100）
      scheduleInterval: 300         # 定时补充调度间隔（秒），0 表示禁用定时补充
      rejectionPolicy: BLOCK        # RingBuffer 满时的拒绝策略：BLOCK / DISCARD

    # ── BidGenerator ─────────────────────────────────────────────────
    bid:
      initialMapCapacity: 512       # id_config 内存缓存初始容量（业务键数量预估）

# Redis 模式下必须开启
xcan:
  redis:
    enabled: true
```

### 配置说明

| 配置项                       | 默认值        | 说明                                              |
|---------------------------|------------|-------------------------------------------------|
| `uid.timeBits`            | 28         | 时间精度为秒，28 bit 约可用 8.7 年                         |
| `uid.workerBits`          | 22         | 决定最大节点数，与 `seqBits` 之和需 ≤ 63                    |
| `uid.seqBits`             | 13         | 每秒最大序列数 = `2^seqBits`                           |
| `uid.epochStr`            | 2016-05-20 | 修改后需重置 `angus_instance` 表，否则 workerId 对应关系错位    |
| `cached.boostPower`       | 2          | 值为 2 时 RingBuffer 约有 32768 槽，约占 256KB 内存（每槽 8B） |
| `cached.paddingFactor`    | 50         | 50 表示剩余 50% 即开始补充，降低瓶颈风险                        |
| `cached.scheduleInterval` | 300        | 防止低流量时 Buffer 长期不补充导致突发流量时来不及填充                 |
| `bid.initialMapCapacity`  | 512        | 影响 HashMap 初次扩容次数，按业务键数量预估                      |

---

## 6. 三方接入说明

### 6.1 前置条件

1. 引入 Starter 依赖：

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.idgen-starter</artifactId>
  <version>3.0.0</version>
</dependency>
```

2. 开启自动装配：

```yaml
angus:
  idgen:
    enabled: true
```

3. 配置数据源（JPA entity 扫描 + DDL 初始化）：

脚本文件位置：

- `idgen/core/src/main/resources/schema/mysql/idgen-schema.sql`
- `idgen/core/src/main/resources/schema/postgres/idgen-schema.sql`

```yaml
angus:
  datasource:
    extra:
      entityPackages[0]: cloud.xcan.angus.idgen.entity
    mysql:
      schema[0]: schema/mysql/idgen-schema.sql   # 应用启动时若表不存在则自动执行
    postgresql:
      schema[0]: schema/postgres/idgen-schema.sql
```

### 6.2 场景一：使用 UidGenerator 生成 64 位分布式 ID

适用于需要高性能、唯一、时序有序的内部 ID 场景（如数据库主键）。

**注入方式（业务层）**：

```java
@Service
public class OrderService {

    @Resource
    private CachedUidGenerator uidGenerator;

    public Order createOrder() {
        long uid = uidGenerator.getUID();
        // uid 是 64 位 long，全局唯一且时序递增
        Order order = new Order();
        order.setId(uid);
        return orderRepository.save(order);
    }
}
```

**解析 UID（调试用）**：

```java
String info = uidGenerator.parseUID(uid);
// 输出示例：{"UID":"6765612809367875584","timestamp":"2024-09-01 10:30:00",
//            "workerId":"1","sequence":"0"}
```

**门面层获取（非 Spring Bean 上下文）**：

```java
CachedUidGenerator generator =
    (CachedUidGenerator) SpringContextHolder.getBean("uidGenerator");
long uid = generator.getUID();
```

### 6.3 场景二：使用 BidGenerator（DB 模式）生成可读业务 ID

适用于订单号、合同编号等需要可读格式的业务场景，持久化到 `angus_id_config` 表。

**第一步：在 `angus_id_config` 表插入配置**：

```sql
-- PREFIX_DATE_SEQ 格式，DB 模式，平台唯一，步长 5000
INSERT INTO `id_config`
  (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`,
   `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `modified_date`)
VALUES
  (UUID(), 'order', 'PREFIX_DATE_SEQ', 'ORD', 'YYYYMMDD', 10,
   'DB', 'PLATFORM', -1, 0, 5000, NOW(), NOW());
```

生成示例：`ORD202409010000000001`（前缀 ORD + 日期 20240901 + 10 位序号）

**第二步：注入并调用**：

```java
@Service
public class OrderService {

    @Resource
    private BidGenerator bidGenerator;

    public String generateOrderNo() {
        return bidGenerator.getId("order");
        // 返回：ORD202409010000000001
    }

    public List<String> batchGenerateOrderNos(int count) {
        return bidGenerator.getIds("order", count);
        // 一次批量获取，内部仅取一次号段，性能高效
    }
}
```

### 6.4 场景三：使用 BidGenerator（Redis 模式）

适用于多实例部署、不需要号段缓存、追求强一致性的场景。

```yaml
angus:
  idgen:
    enabled: true
xcan:
  redis:
    enabled: true
```

```sql
-- SEQ 格式，Redis 模式，平台唯一
INSERT INTO `id_config`
  (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`,
   `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `modified_date`)
VALUES
  (UUID(), 'invoice', 'DATE_SEQ', '', 'YYYYMMDD', 8,
   'REDIS', 'PLATFORM', -1, 0, 1, NOW(), NOW());
```

> Redis 模式下 `step` 参数无效（因为不使用本地号段缓存），填 1 即可。

```java
String invoiceNo = bidGenerator.getId("invoice");
// 返回：2024090100000001
```

### 6.5 场景四：多租户业务 ID（TENANT 范围）

每个租户的编号相互独立，互不影响（如不同公司的工单号从 1 开始）。

```sql
-- 插入模板行（tenantId = -1），首次调用时自动克隆为各租户独立配置
INSERT INTO `id_config`
  (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`,
   `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `modified_date`)
VALUES
  (UUID(), 'ticket', 'PREFIX_SEQ', 'TK', '', 8,
   'DB', 'TENANT', -1, 0, 2000, NOW(), NOW());
```

```java
// 不同租户获取独立序列号
String ticket1 = bidGenerator.getId("ticket", 1001L);  // TK00000001（租户1001）
String ticket2 = bidGenerator.getId("ticket", 2002L);  // TK00000001（租户2002，独立序列）
```

### 6.6 场景五：批量获取业务 ID

```java
// 批量获取 100 个订单号（内部仅进行一次或少量号段取值操作）
List<String> orderNos = bidGenerator.getIds("order", 100);

// 带租户的批量获取
List<String> ticketNos = bidGenerator.getIds("ticket", 50, 1001L);
```

> 批量上限：`BidGenerator.MAX_BATCH_NUM = 10000`。

### 6.7 自定义 WorkerId 分配（可选）

默认使用 `DisposableInstanceIdAssigner` 通过 `angus_instance` 表自动分配。如需自定义（如 ZooKeeper
分配），实现 SPI：

```java
@Bean
@ConditionalOnMissingBean
public InstanceIdAssigner myInstanceIdAssigner() {
    return new InstanceIdAssigner() {
        @Override
        public long assignInstanceIdByEnv() {
            // 从 ZooKeeper / Consul / 配置中心读取唯一节点 ID
            return zkClient.getNodeId();
        }

        @Override
        public long assignInstanceIdByParam(String host, String port, String type) {
            return zkClient.getNodeId(host, port);
        }
    };
}
```

> 注：因框架使用 `@ConditionalOnMissingBean`，自定义 Bean 会自动替换默认实现。

---

## 7. 性能参考

| 生成器                   | 模式             | 单机 QPS     | 说明                            |
|-----------------------|----------------|------------|-------------------------------|
| `DefaultUidGenerator` | 同步             | ~50 万      | synchronized nextId()，受时钟精度限制 |
| `CachedUidGenerator`  | RingBuffer     | **~600 万** | 无锁消费，生产与消费并行                  |
| `BidGenerator`        | DB 模式（步长 5000） | ~10–50 万   | 取决于号段步长和网络延迟                  |
| `BidGenerator`        | Redis 模式       | ~20–100 万  | INCRBY 原子操作，取决于 Redis 延迟      |

- [UidGenerator 性能详细数据](docs/UIDPerformance_zh.md)
- [BidGenerator 性能详细数据](docs/BIDPerformance_zh.md)

---

## 8. 注意事项

1. **位分配不可在运行中修改**：`timeBits`/`workerBits`/`seqBits` 更改后，原有 ID 解析会错乱，需要全量迁移。
2. **epochStr 迁移风险**：修改 epoch 基准日期会导致生成的 ID 从更小值重新开始，可能与历史 ID 重叠。
3. **instance 表唯一约束**：同一 `host:port` 重启后复用原 workerId，更换部署地址时会产生新行。
4. **CachedUidGenerator 内存消耗**：默认 boostPower=2 时 RingBuffer 约占 256KB（`32768 × 8B`
   ），生产环境按需调整。
5. **BidGenerator 号段丢失**：应用宕机或重启时，内存中未消费的号段会丢失，产生断号，属于正常现象。
6. **TENANT 范围首次调用延迟**：首次为新租户克隆配置时有一次 DB 写入，后续复用缓存。
7. **id_config 表自动创建**：受 `xcan.datasource.mysql.schema` 配置控制，无需手动建表。
8. **不要在 `id_config` 中直接修改 `max_id`**：并发场景下手动改值可能导致 ID 重复。
