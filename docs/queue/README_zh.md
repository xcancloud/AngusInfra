# 数据库消息队列（angus-infra-queue）

[English](README.md) | [中文](README_zh.md)

以关系库（JPA）为后端的持久化消息队列，提供类 SQS 的**租约消费**语义，无需 Kafka / RabbitMQ。
语义为 **at-least-once**，业务侧需幂等。

> **不适合：** 超高吞吐 / 跨机房流式管道（请用专业 MQ）；强 precisely-once（本模块不保证）。

---

## 目录

1. [概述](#1-概述)
2. [架构](#2-架构)
3. [数据模型](#3-数据模型)
4. [配置项](#4-配置项)
5. [接入指南](#5-接入指南)
6. [API 参考](#6-api-参考)
7. [租约 / DLQ / 调度](#7-租约--dlq--调度)
8. [管理 REST](#8-管理-rest)
9. [最佳实践](#9-最佳实践)
10. [排查指南](#10-排查指南)

---

## 1. 概述

| 组件 | 职责 |
|------|------|
| `QueueService` | 发送 / 租约 / ACK / NACK |
| `QueueAdminService` | Topic 统计、回收、清理、DLQ 重放 |
| `RepositoryAdapter` | 持久化 SPI（默认 JPA） |
| `LeaseReaperScheduler` | 回收过期租约 |
| `DeadLetterMoverScheduler` | 超限消息迁入死信 |
| `queue-web` | 可选 REST（不在 starter 自动装配内） |

**设计原则**

1. **DB-backed、无中间件** — 消息落在 `angus_mq_message` / `angus_mq_dead_letter`。
2. **core 与 Spring 解耦** — 经 `RepositoryAdapter` SPI。
3. **At-least-once** — lease + 超时 reclaim；消费方必须幂等。
4. **分区内有序** — `(topic, partitionKey)` 确定性哈希。
5. **管理面可选** — REST 在独立 `queue-web` 模块。

---

## 2. 架构

```
Producer → QueueService.send → RepositoryAdapter → angus_mq_message (READY)
Consumer → lease (READY→LEASED) → listLeasedByOwner → ack (DONE) | nack (READY + backoff)
LeaseReaperScheduler     → LEASED & lease_until < NOW → READY
DeadLetterMoverScheduler → attempts >= max_attempts → DLQ + delete message
Admin → purgeDone / purge DLQ / replay
```

状态 ordinal：`READY=0`，`LEASED=1`，`DONE=2`。  
租约排序：`ORDER BY priority DESC, visible_at ASC, id ASC`。

---

## 3. 数据模型

脚本：`classpath:schema/mysql/queue-schema.sql`（Postgres 同目录）

### 3.1 `angus_mq_message`

| 列 | 说明 |
|----|------|
| `id` | PK |
| `topic` | 主题 |
| `partition_id` | 分区 |
| `priority` | 优先级，默认 0 |
| `payload` / `headers` | JSON |
| `status` | 0/1/2 |
| `visible_at` | 延迟可见时间 |
| `lease_until` / `lease_owner` | 租约 |
| `attempts` / `max_attempts` | 默认 max=16 |
| `idempotency_key` | 仅存储，**无唯一索引、无强制去重** |
| `version` | `@Version`（原生 UPDATE 不碰） |

### 3.2 `angus_mq_dead_letter`

| 列 | 说明 |
|----|------|
| `id`, `topic`, `partition_id`, `payload`, `headers` | 死信内容 |
| `attempts`, `reason` | 如 `max_attempts_exceeded` |
| `created_at`, `deleted_at` | 软删时间戳 |

---

## 4. 配置项

前缀：`angus.queue`

### 4.1 `QueueProperties` 绑定字段

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `partitions` | `8` | REST 发送默认分区数 |
| `poll-batch` | `100` | 默认拉取批量 |
| `ack-batch` | `200` | 已绑定，**当前未使用** |
| `lease-seconds` | `30` | 默认租约秒数 |
| `reclaim-batch` | `500` | 回收批量 |
| `dead-letter-move-batch` | `200` | DLQ 迁移批量 |
| `scheduling.pool-size` | `4` | 调度线程池 |
| `scheduling.thread-name-prefix` | `queue-scheduler-` | 线程名前缀 |
| `admin.retention-days` | `7` | 软删 DLQ 保留天数 |
| `admin.purge-interval-ms` | `600000` | 软删物理清理间隔 |

### 4.2 条件注解 / `@Scheduled` 占位符属性

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `scheduling.enabled` | `true` | 是否注册 LeaseReaper / DeadLetterMover |
| `admin.soft-delete-dlq` | `false` | 软删 DLQ + Purger |
| `reclaim-interval-ms` | `3000` | 租约回收间隔 |
| `dead-letter-move-interval-ms` | `5000` | DLQ 迁移间隔 |

```yaml
angus:
  queue:
    partitions: 8
    poll-batch: 100
    lease-seconds: 30
    reclaim-batch: 500
    dead-letter-move-batch: 200
    reclaim-interval-ms: 3000
    dead-letter-move-interval-ms: 5000
    scheduling:
      enabled: true
      pool-size: 4
    admin:
      soft-delete-dlq: false
      retention-days: 7
      purge-interval-ms: 600000
```

---

## 5. 接入指南

### 5.1 依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.queue-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

需要 REST 时再加 `xcan-angusinfra.queue-web`，并扫描 `cloud.xcan.angus.queue.web`，
且具备 Security + `PPS` Bean。

执行 schema，`ddl-auto=validate`。

### 5.2 自动装配

`QueueAutoConfiguration`（`@ConditionalOnClass(JpaRepository.class)`）创建：

- `RepositoryAdapter` → `JpaRepositoryAdapter`
- `QueueService` / `QueueAdminService`
- `queueTaskScheduler`
- `LeaseReaperScheduler` / `DeadLetterMoverScheduler`（`scheduling.enabled`）
- `DlqSoftDeletePurgerScheduler`（仅 `soft-delete-dlq=true`）

**Controllers 不由 AutoConfiguration 创建。**

### 5.3 发送

```java
queueService.send(SendMessage.builder()
    .topic("order-events")
    .partitionKey("order-" + orderId)
    .payload("{\"orderId\":1}")
    .priority(10)
    .maxAttempts(5)
    .numPartitions(8)   // 重要：default send() 省略时为 1，不是 properties.partitions
    .idempotencyKey("order-1-PAID")
    .build());
```

### 5.4 消费

```java
int n = queueService.lease("order-events", List.of(0, 1), owner, 30, 50);
List<MessageData> msgs = queueService.listLeasedByOwner(owner, 50);
queueService.ack(ids);
// 或
queueService.nack(ids, 5); // backoff 秒
```

多节点应分配互斥 partition 集合。

---

## 6. API 参考

### `QueueService`

| 方法 | 说明 |
|------|------|
| `send(...)` / `send(SendMessage)` | 发送；default 的 `numPartitions=1` |
| `lease(...)` / `lease(LeaseMessages)` | 租约；default lease=30、limit=100 |
| `listLeasedByOwner(owner, limit)` | 列出本 owner 未过期租约 |
| `ack(ids)` / `nack(ids, backoffSec)` | 确认 / 退回（attempts+1） |
| `moveExceededAttemptsToDeadLetter(limit)` | 手动迁 DLQ |

### `QueueAdminService`

`listTopics` / `topicStats` / `reclaimExpired` / `purgeDone` /
`purgeDeadLetters` / `replayFromDeadLetter`

---

## 7. 租约 / DLQ / 调度

| 行为 | 说明 |
|------|------|
| lease | READY 且 `visible_at<=now` → LEASED |
| ack | → DONE |
| nack | → READY，`attempts+1`，延迟 `backoff` |
| LeaseReaper | 过期 LEASED → READY，**不增加 attempts** |
| DeadLetterMover | `attempts >= max_attempts` → 写 DLQ 并删主表行 |
| Replay | 新 READY（attempts=0, max=16）；**不保留**原 idempotency_key |
| 软删 Purger | 物理删除超保留期的 `deleted_at` 行 |

`scheduling.enabled=false` 时不注册 Reaper/Mover；`TaskScheduler` Bean 仍创建。  
Purger 只看 `soft-delete-dlq`，不看 `scheduling.enabled`。

---

## 8. 管理 REST

模块：`queue-web`  
鉴权：`@PPS.isCloudTenantSecurity() && @PPS.isSysAdmin()`

**Queue** `/api/v1/queue`：`POST /send`、`/poll`、`/ack`、`/nack`  
**Admin** `/api/v1/queue/admin`：`GET /topics`、`/stats`；`POST /reclaim`、`/dlq/replay`；
`DELETE /purge/done`、`/purge/dlq`

REST 发送使用 `angus.queue.partitions`；NACK 默认 `backoffSeconds=5`。

---

## 9. 最佳实践

1. 消费逻辑必须幂等（租约超时会重投）。
2. 程序化 `send` 显式设置 `numPartitions`，与消费者分区规划一致。
3. 多实例消费分配互斥 partitions。
4. 不要依赖 `idempotency_key` 做强制去重（需业务自建唯一约束或查重）。
5. 生产用 schema SQL + `validate`；定期 `purgeDone`。
6. 调大 `lease-seconds` 覆盖最坏处理时间，避免被 Reaper 抢走。

---

## 10. 排查指南

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 消息重复消费 | 未 ACK / 租约超时 | 幂等处理；加大 lease |
| 消息不出现 | `visible_at` 未到 / 分区不对 | 查字段与 partition |
| 进 DLQ 过快 | `maxAttempts` 过小 / 频繁 nack | 调参与修消费逻辑 |
| 调度不跑 | `scheduling.enabled=false` | 打开或手动 reclaim |
| REST 404 | 未引入 queue-web | 加依赖并扫描 |
| 程序化分区全是 0 | `numPartitions` 默认 1 | 显式传分区数 |

---

## 相关类

| 类 | 模块 |
|----|------|
| `cloud.xcan.angus.queue.service.QueueService` | `queue-core` |
| `cloud.xcan.angus.queue.service.QueueAdminService` | `queue-core` |
| `cloud.xcan.angus.queue.spi.RepositoryAdapter` | `queue-core` |
| `cloud.xcan.angus.queue.autoconfigure.QueueAutoConfiguration` | `queue-starter` |
| `cloud.xcan.angus.queue.web.QueueController` | `queue-web` |
