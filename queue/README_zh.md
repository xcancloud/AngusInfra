# AngusInfra — 队列模块

## 一、模块概述

`queue` 模块是 AngusInfra 基础设施框架提供的**数据库驱动的持久化消息队列**组件。它不依赖 Kafka、RabbitMQ 等外部中间件，而是将关系型数据库（JPA）作为消息存储后端，提供类 SQS 的租约式消费语义，适用于已有数据库且不愿引入额外中间件的场景。

### 核心能力

| 能力 | 说明 |
|------|------|
| **多 Topic** | 每个 topic 独立管理，互不干扰 |
| **分区** | 每个 topic 支持可配置分区（默认 8），保证相同 key 路由到同一分区 |
| **优先级** | 消息支持整数优先级，高优先级消息优先被租约 |
| **延迟消息** | 通过 `visibleAt` 字段支持定时/延迟投递 |
| **租约式消费** | 消费者租约消息（至少一次语义），租约超时自动回收 |
| **ACK / NACK** | 处理成功 ACK 标记完成，失败 NACK 带退避时间重新入队 |
| **死信队列（DLQ）** | 超过最大重试次数的消息自动转移到 DLQ，支持重放和清除 |
| **软删除 DLQ** | 可选软删除模式，按保留天数定期物理清除 |
| **幂等 Key** | 可选的去重 key，防止重复投递 |
| **审计日志** | 管理操作自动记录审计日志（可自定义实现） |

---

## 二、架构设计

### 模块结构

```
queue/
├── core/          # 核心接口与领域逻辑（无 Spring 强依赖）
│   └── src/main/java/cloud/xcan/angus/queue/core/
│       ├── service/
│       │   ├── QueueService.java              # 队列操作主接口（发送/租约/ACK/NACK）
│       │   ├── DefaultQueueService.java        # QueueService 默认实现
│       │   ├── QueueAdminService.java          # 管理接口（统计/回收/清除/重放）
│       │   ├── DefaultQueueAdminService.java   # QueueAdminService 默认实现
│       │   ├── AuditLogger.java                # 审计日志接口
│       │   └── Slf4jAuditLogger.java           # 基于 SLF4J 的默认实现
│       ├── spi/
│       │   ├── RepositoryAdapter.java          # 持久化 SPI（核心扩展点）
│       │   └── SoftDeleteDlqSupport.java       # 可选：软删除 DLQ 扩展接口
│       ├── entity/
│       │   ├── MessageEntity.java              # 消息 JPA 实体（表：angus_mq_message）
│       │   ├── DeadLetterEntity.java           # 死信 JPA 实体（表：angus_mq_dead_letter）
│       │   └── MessageStatus.java              # 消息状态枚举：READY/LEASED/DONE
│       ├── model/
│       │   ├── SendMessage.java                # 发送消息请求 DTO
│       │   ├── MessageData.java                # 消息数据响应 DTO
│       │   ├── LeaseMessages.java              # 租约请求 DTO
│       │   └── DeadLetterData.java             # 死信数据 DTO
│       ├── scheduler/
│       │   ├── LeaseReaper.java                # 过期租约回收器（核心逻辑）
│       │   └── DeadLetterMover.java            # 超重试消息 DLQ 转移器（核心逻辑）
│       └── util/
│           └── Partitioner.java                # (topic, key) → partition 哈希
└── starter/       # Spring Boot 自动配置
    └── src/main/java/cloud/xcan/angus/queue/starter/
        ├── QueueAutoConfiguration.java          # Spring Boot 自动配置入口
        ├── adapter/
        │   └── JpaRepositoryAdapter.java        # RepositoryAdapter 的 JPA 实现
        ├── autoconfigure/
        │   └── QueueProperties.java             # 配置属性绑定
        ├── repository/
        │   ├── MessageRepository.java           # Spring Data JPA + 原生 SQL
        │   └── DeadLetterRepository.java        # Spring Data JPA + 原生 SQL
        ├── scheduler/
        │   ├── LeaseReaperScheduler.java        # @Scheduled 包装 LeaseReaper
        │   ├── DeadLetterMoverScheduler.java    # @Scheduled 包装 DeadLetterMover
        │   └── DlqSoftDeletePurgerScheduler.java# 软删除 DLQ 物理清理调度
        ├── service/
        │   └── AdminService.java
        └── web/
            ├── QueueController.java             # 队列 REST API：send/poll/ack/nack
            └── AdminController.java             # 管理 REST API：stats/reclaim/purge/replay
```

### 消息生命周期

```
                    ┌─────────────────────────────────────────┐
                    │              send() 发送消息              │
                    └───────────────────┬─────────────────────┘
                                        │
                                        ▼
                              ┌─────────────────┐
                              │   READY          │◄──────────────────┐
                              │  status = 0      │                   │
                              │  (可见且未被租约) │                   │ nack() 退避重入
                              └────────┬─────────┘                   │
                                       │                             │
                              lease() 租约                           │
                                       │                             │
                                       ▼                             │
                              ┌─────────────────┐                   │
                              │   LEASED         ├───────────────────┘
                              │  status = 1      │
                              │  (正在被消费)    │──── 租约超时 ──► LeaseReaper 回收 → READY
                              └────────┬─────────┘
                                       │
                              ack() 确认成功
                                       │
                                       ▼
                              ┌─────────────────┐
                              │   DONE           │
                              │  status = 2      │──── purgeDone() 定期清理
                              └─────────────────┘

                    attempts >= maxAttempts
                              │
                              ▼
                    ┌─────────────────────┐
                    │   Dead Letter Queue  │──── replay() 重放 → READY
                    │   angus_mq_dead_letter     │──── purge() 清除（硬删除或软删除）
                    └─────────────────────┘
```

### 分区与路由

```java
// 分区计算：基于 topic + partitionKey 的确定性哈希
public static int partition(String topic, String key, int numPartitions) {
    String base = topic + "#" + (key == null ? "" : key);
    int h = base.hashCode();
    return Math.floorMod(h, Math.max(1, numPartitions));
}
```

相同 `partitionKey` 的消息始终路由到同一分区，保证**分区内顺序消费**。消费者可选择订阅部分分区（分区消费者组负载均衡）。

---

## 三、核心组件详解

### 3.1 `QueueService` — 队列操作主接口

```java
public interface QueueService {
    // 发送消息，返回消息 ID
    Long send(String topic, String partitionKey, String payload,
              String headers, int priority, Instant visibleAt,
              String idempotencyKey, int maxAttempts, int numPartitions);

    // 便捷方法：接收 SendMessage DTO
    default Long send(SendMessage req) { ... }

    // 租约消息：将 READY 消息锁定为 LEASED，返回租约数量
    int lease(String topic, Collection<Integer> partitions,
              String owner, int leaseSec, int limit);

    // 查询当前 owner 持有的 LEASED 消息列表
    List<MessageData> listLeasedByOwner(String owner, int limit);

    // 确认消息处理成功，标记为 DONE
    int ack(Collection<Long> ids);

    // 拒绝消息，带退避时间重新入队（LEASED → READY，attempts+1）
    int nack(Collection<Long> ids, int backoffSec);

    // 将超出最大重试次数的消息转移到 DLQ
    int moveExceededAttemptsToDeadLetter(int limit);
}
```

### 3.2 `QueueAdminService` — 管理操作接口

```java
public interface QueueAdminService {
    // 获取 topic 的统计信息（各状态数量、DLQ 数量、每分区 READY 数）
    Map<String, Object> topicStats(String topic);

    // 回收超时未 ACK 的 LEASED 消息（LEASED → READY）
    int reclaimExpired(int limit);

    // 清除指定 topic 中指定时间前的 DONE 消息
    int purgeDone(String topic, Instant before);

    // 清除 DLQ 消息（硬删除或软删除，由配置决定）
    int purgeDeadLetters(String topic);

    // 从 DLQ 重放消息回主队列（READY）
    int replayFromDeadLetter(String topic, int limit);
}
```

### 3.3 `RepositoryAdapter` — 持久化 SPI

核心扩展点，`core` 模块不依赖任何 Spring / JPA，所有持久化操作通过此接口解耦：

```java
public interface RepositoryAdapter {
    Long saveMessage(SendMessage msg);
    int leaseBatch(String topic, Collection<Integer> partitions, String owner, int leaseSec, int limit);
    List<MessageData> findLeasedByOwner(String owner, int limit);
    int ackBatch(Collection<Long> ids);
    int nackBatch(Collection<Long> ids, int backoffSec);
    int moveExceededToDeadLetter(int limit);
    int reclaimExpiredLeases(int limit);
    List<StatusCount> countByStatus(String topic);
    List<PartitionCount> readyCountPerPartition(String topic);
    int purgeDoneBefore(String topic, Instant before);
    long deadLetterCountByTopic(String topic);
    List<DeadLetterData> findDeadLettersByTopicLimit(String topic, int limit);
    List<Long> saveRecoveredMessages(Collection<DeadLetterData> items);
    int deleteDeadLettersByIds(Collection<Long> ids);
    int purgeDeadLettersByTopic(String topic);
}
```

框架提供 `JpaRepositoryAdapter`（Spring Data JPA 实现），用户也可自定义实现替换。

### 3.4 数据库表结构

**`angus_mq_message`** — 主消息表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `topic` | VARCHAR(128) | 消息主题 |
| `partition_id` | INT | 分区 ID |
| `priority` | INT | 优先级（越大越优先，默认 0） |
| `payload` | JSON | 消息体 |
| `headers` | JSON | 消息头（可选） |
| `status` | TINYINT | 0=READY, 1=LEASED, 2=DONE |
| `visible_at` | TIMESTAMP | 可见时间（支持延迟消息） |
| `lease_until` | TIMESTAMP | 租约到期时间 |
| `lease_owner` | VARCHAR(128) | 持有租约的消费者标识 |
| `attempts` | INT | 已尝试消费次数 |
| `max_attempts` | INT | 最大重试次数（默认 16） |
| `idempotency_key` | VARCHAR(256) | 幂等 key（可选） |
| `created_at` | TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | 更新时间 |
| `version` | BIGINT | 乐观锁版本号 |

索引：
- `idx_mq_msg_topic_status_visible`（topic, status, visible_at）— 租约查询核心索引
- `idx_mq_msg_status_lease_until`（status, lease_until）— 租约回收查询
- `idx_mq_msg_lease_owner`（lease_owner）— 按 owner 查询已租约消息
- `idx_mq_msg_attempts`（attempts）— 超限重试消息转移查询

**`angus_mq_dead_letter`** — 死信队列表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `topic` | VARCHAR(128) | 消息主题 |
| `partition_id` | INT | 分区 ID |
| `payload` | JSON | 消息体 |
| `headers` | JSON | 消息头 |
| `attempts` | INT | 进入 DLQ 时的尝试次数 |
| `reason` | VARCHAR(256) | 进入 DLQ 的原因 |
| `created_at` | TIMESTAMP | 创建时间 |
| `deleted_at` | TIMESTAMP | 软删除时间（NULL 表示未删除） |

索引：
- `idx_mq_dlq_topic`（topic）— topic 级查询
- `idx_mq_dlq_deleted_at`（deleted_at）— 软删除记录定期清理

### 3.5 后台调度器

| 调度器 | 默认间隔 | 配置项 | 功能 |
|--------|---------|--------|------|
| `LeaseReaperScheduler` | 3 秒 | `angus.queue.reclaim-interval-ms` | 将超时 LEASED 消息回收为 READY |
| `DeadLetterMoverScheduler` | 5 秒 | `angus.queue.dead-letter-move-interval-ms` | 将超出 maxAttempts 的消息移入 DLQ |
| `DlqSoftDeletePurgerScheduler` | 10 分钟 | `angus.queue.admin.purge-interval-ms` | 物理删除超出保留期的软删除 DLQ 记录 |

### 3.6 自动配置条件装配

```
classpath 存在 JpaRepository（Spring Data JPA）?
    └─ 是 → 启动 QueueAutoConfiguration
        ├─ RepositoryAdapter     = JpaRepositoryAdapter（JPA 实现）
        ├─ QueueService          = DefaultQueueService
        ├─ QueueAdminService     = DefaultQueueAdminService
        │     angus.queue.admin.soft-delete-dlq = true ?
        │         └─ 是 → 启用软删除模式 + DlqSoftDeletePurgerScheduler
        ├─ AuditLogger           = Slf4jAuditLogger（可覆盖）
        ├─ TaskScheduler         = ThreadPoolTaskScheduler（线程数可配）
        ├─ LeaseReaperScheduler      （angus.queue.scheduling.enabled != false）
        ├─ DeadLetterMoverScheduler  （angus.queue.scheduling.enabled != false）
        ├─ QueueController           （REST: /api/v1/queue）
        └─ AdminController           （REST: /api/v1/queue/admin）
```

---

## 四、配置参考

所有配置项以 `angus.queue` 为前缀，对应 `QueueProperties` 类：

```yaml
angus:
  queue:
    partitions: 8                    # 每个 topic 的分区数（发送时使用），默认 8
    poll-batch: 100                  # 每次 poll 最大租约消息数，默认 100
    ack-batch: 200                   # 每次批量 ACK 最大条数，默认 200
    lease-seconds: 30                # 默认租约时长（秒），默认 30
    reclaim-batch: 500               # LeaseReaper 每次回收最大条数，默认 500
    dead-letter-move-batch: 200      # DeadLetterMover 每次转移最大条数，默认 200

    # 调度器配置
    reclaim-interval-ms: 3000        # LeaseReaper 执行间隔（ms），默认 3000
    dead-letter-move-interval-ms: 5000  # DeadLetterMover 执行间隔（ms），默认 5000

    scheduling:
      enabled: true                  # 是否启用后台调度器，默认 true
      pool-size: 4                   # 调度线程池大小，默认 4
      thread-name-prefix: "queue-scheduler-"  # 线程名前缀

    admin:
      soft-delete-dlq: false         # 是否使用软删除模式（DLQ 先标 deleted_at，再定期物理删除），默认 false
      retention-days: 7              # 软删除 DLQ 保留天数，默认 7 天
      purge-interval-ms: 600000      # 软删除 DLQ 物理清理间隔（ms），默认 10 分钟
```

---

## 五、REST API

### 5.1 队列操作 API（`/api/v1/queue`）

| Method | Path | 说明 |
|--------|------|------|
| `POST` | `/api/v1/queue/send` | 发送消息到指定 topic |
| `POST` | `/api/v1/queue/poll` | 租约消息（READY → LEASED），返回已持有消息列表 |
| `POST` | `/api/v1/queue/ack` | 确认消息处理成功（LEASED → DONE） |
| `POST` | `/api/v1/queue/nack` | 拒绝消息并退避重入（LEASED → READY，attempts+1） |

**发送消息（send）请求体：**

```json
{
  "topic": "order-events",
  "partitionKey": "order-123",
  "payload": "{\"orderId\":123,\"status\":\"PAID\"}",
  "headers": "{\"source\":\"payment-service\"}",
  "priority": 10,
  "visibleAt": "2026-04-01T10:00:00Z",
  "idempotencyKey": "payment-abc-xyz",
  "maxAttempts": 5
}
```

**Poll（租约）请求体：**

```json
{
  "topic": "order-events",
  "owner": "worker-node-1",
  "partitions": [0, 1, 2, 3],
  "leaseSeconds": 60,
  "limit": 50
}
```

**ACK 请求体：**

```json
{ "ids": [101, 102, 103] }
```

**NACK 请求体：**

```json
{ "ids": [104, 105], "backoffSeconds": 30 }
```

### 5.2 管理 API（`/api/v1/queue/admin`）

| Method | Path | 说明 |
|--------|------|------|
| `GET` | `/api/v1/queue/admin/stats?topic=<t>` | 获取 topic 统计（状态分布、DLQ 数、每分区 READY 数） |
| `POST` | `/api/v1/queue/admin/reclaim?limit=500` | 手动触发超时租约回收 |
| `DELETE` | `/api/v1/queue/admin/purge/done?topic=<t>&before=<iso>` | 清除指定时间前的 DONE 消息 |
| `DELETE` | `/api/v1/queue/admin/purge/dlq?topic=<t>` | 清除 DLQ 消息（硬/软由配置决定） |
| `POST` | `/api/v1/queue/admin/dlq/replay?topic=<t>&limit=100` | 从 DLQ 重放消息回主队列 |

---

## 六、三方接入说明

### 6.1 引入依赖

```xml
<!-- queue starter：包含核心逻辑 + Spring Boot 自动配置 -->
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.queue-starter</artifactId>
  <version>3.0.0</version>
</dependency>

<!-- Spring Data JPA（必须，自动配置依赖 JpaRepository） -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- 数据库驱动（以 MySQL 为例） -->
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
</dependency>
```

### 6.2 配置数据源

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC
    username: root
    password: yourpassword
  jpa:
    hibernate:
      ddl-auto: update     # 开发环境自动建表；生产建议 validate + 手动建表

angus:
  queue:
    partitions: 8
    lease-seconds: 30
```

### 6.3 手动建表（生产推荐）

```sql
-- 主消息表
CREATE TABLE IF NOT EXISTS angus_mq_message (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    topic           VARCHAR(128) NOT NULL,
    partition_id    INT          NOT NULL,
    priority        INT          NOT NULL DEFAULT 0,
    payload         JSON         NOT NULL,
    headers         JSON,
    status          TINYINT      NOT NULL DEFAULT 0,  -- 0=READY,1=LEASED,2=DONE
    visible_at      TIMESTAMP(6) NOT NULL,
    lease_until     TIMESTAMP(6),
    lease_owner     VARCHAR(128),
    attempts        INT          NOT NULL DEFAULT 0,
    max_attempts    INT          NOT NULL DEFAULT 16,
    idempotency_key VARCHAR(256),
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_mq_msg_topic_status_visible (topic, status, visible_at),
    INDEX idx_mq_msg_status_lease_until (status, lease_until),
    INDEX idx_mq_msg_lease_owner (lease_owner),
    INDEX idx_mq_msg_attempts (attempts)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 死信队列表
CREATE TABLE IF NOT EXISTS angus_mq_dead_letter (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    topic        VARCHAR(128) NOT NULL,
    partition_id INT          NOT NULL,
    payload      JSON         NOT NULL,
    headers      JSON,
    attempts     INT          NOT NULL,
    reason       VARCHAR(256),
    created_at   TIMESTAMP(6) NOT NULL,
    deleted_at   TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_mq_dlq_topic (topic),
    INDEX idx_mq_dlq_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 6.4 注入使用

自动配置完成后，直接注入 `QueueService` 和 `QueueAdminService`：

```java
@Service
public class OrderEventService {

    private final QueueService queueService;

    public OrderEventService(QueueService queueService) {
        this.queueService = queueService;
    }

    // ===== 生产者：发送消息 =====
    public void sendOrderEvent(Long orderId, String status) {
        String payload = """
            {"orderId":%d,"status":"%s"}
            """.formatted(orderId, status);

        Long msgId = queueService.send(SendMessage.builder()
            .topic("order-events")
            .partitionKey("order-" + orderId)  // 同一订单路由到同一分区，保证顺序
            .payload(payload)
            .headers("{\"source\":\"order-service\"}")
            .priority(5)
            .maxAttempts(10)
            .idempotencyKey("order-" + orderId + "-" + status)
            .build());

        log.info("Sent order event, msgId={}", msgId);
    }

    // ===== 延迟消息：5 分钟后可见 =====
    public void sendDelayedReminder(Long orderId) {
        queueService.send(SendMessage.builder()
            .topic("order-reminders")
            .partitionKey("order-" + orderId)
            .payload("{\"orderId\":" + orderId + "}")
            .visibleAt(Instant.now().plusSeconds(300))  // 5 分钟后投递
            .build());
    }
}
```

### 6.5 消费者：轮询处理（程序化方式）

```java
@Component
public class OrderEventConsumer {

    private final QueueService queueService;
    private static final String OWNER = "order-consumer-" + InetAddress.getLocalHost().getHostName();

    // 每 2 秒轮询一次
    @Scheduled(fixedDelay = 2000)
    public void poll() {
        // 1. 租约消息（READY → LEASED）
        queueService.lease("order-events",
            List.of(0, 1, 2, 3, 4, 5, 6, 7),  // 订阅所有分区
            OWNER, 60, 50);                       // 租约 60 秒，最多取 50 条

        // 2. 查询本节点持有的消息
        List<MessageData> messages = queueService.listLeasedByOwner(OWNER, 50);

        List<Long> ackIds = new ArrayList<>();
        List<Long> nackIds = new ArrayList<>();

        for (MessageData msg : messages) {
            try {
                processMessage(msg);
                ackIds.add(msg.getId());
            } catch (Exception e) {
                log.warn("Failed to process msg {}, will nack", msg.getId(), e);
                nackIds.add(msg.getId());
            }
        }

        // 3. 批量 ACK / NACK
        if (!ackIds.isEmpty()) queueService.ack(ackIds);
        if (!nackIds.isEmpty()) queueService.nack(nackIds, 30); // 30 秒后重新可见
    }

    private void processMessage(MessageData msg) {
        // 处理业务逻辑...
    }
}
```

### 6.6 场景：启用软删除 DLQ

```yaml
angus:
  queue:
    admin:
      soft-delete-dlq: true     # 开启软删除
      retention-days: 14        # DLQ 记录保留 14 天后物理删除
      purge-interval-ms: 3600000 # 每小时扫描一次
```

软删除模式下，`purgeDeadLetters()` 只将 DLQ 记录的 `deleted_at` 设为当前时间，不立即物理删除；`DlqSoftDeletePurgerScheduler` 定期清理超出保留期的记录，适合需要短暂留存 DLQ 记录用于排查问题的场景。

### 6.7 场景：自定义持久化适配器

如需接入不同存储（如 PostgreSQL 特定优化、MongoDB），实现 `RepositoryAdapter` 接口并注册为 Bean 即可：

```java
@Component
public class MyCustomRepositoryAdapter implements RepositoryAdapter {

    @Override
    public Long saveMessage(SendMessage msg) {
        // 自定义持久化逻辑
    }

    // 实现其余方法...
}
```

自动配置中的 `@ConditionalOnMissingBean(RepositoryAdapter.class)` 确保自定义实现优先于默认 `JpaRepositoryAdapter`。

### 6.8 场景：自定义审计日志

```java
@Component
public class DatabaseAuditLogger implements AuditLogger {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void adminAction(String action, String topic, int affected, String detail) {
        auditLogRepository.save(AuditLog.builder()
            .action(action)
            .topic(topic)
            .affected(affected)
            .detail(detail)
            .timestamp(Instant.now())
            .build());
    }
}
```

同样地，注册该 Bean 后自动配置会跳过 `Slf4jAuditLogger` 的注册。

### 6.9 场景：分区消费者组负载均衡

多节点部署时，可将 topic 的分区均匀分配给各消费者节点，实现并发消费：

```java
// 节点 0：订阅分区 0-3
queueService.lease("order-events", List.of(0, 1, 2, 3), "node-0", 60, 100);

// 节点 1：订阅分区 4-7
queueService.lease("order-events", List.of(4, 5, 6, 7), "node-1", 60, 100);
```

同一 `partitionKey` 路由到固定分区，节点专属分区消费可保证同一 key 的有序处理。

---

## 七、常见问题

**Q: 消息会重复消费吗？**

A: 使用 `lease` 租约模式是**至少一次（at-least-once）**语义。如果消费者在 ACK 之前崩溃，`LeaseReaper` 会在租约超时后将消息重置为 READY，下次会被重新消费。建议消费者实现幂等处理，或发送时设置 `idempotencyKey`。

**Q: 如何调整租约超时时间？**

A: 全局默认通过 `angus.queue.lease-seconds` 设置；也可以在每次 poll 的请求中单独指定 `leaseSeconds` 覆盖全局值。

**Q: DLQ 中的消息如何重放？**

A: 调用 `POST /api/v1/queue/admin/dlq/replay?topic=<t>&limit=100`，或直接调用 `QueueAdminService.replayFromDeadLetter(topic, limit)`。重放会将 DLQ 中的消息重新插入主队列（READY 状态），原 DLQ 记录被删除。

**Q: 调度器会影响应用性能吗？**

A: 调度器使用独立的 `ThreadPoolTaskScheduler`（默认 4 线程），不占用业务线程池。每次调度批量数量通过 `reclaim-batch` / `dead-letter-move-batch` 限制，可根据数据量调整。如需完全禁用调度器（自行管理），设置 `angus.queue.scheduling.enabled=false`。

---

## 八、构建与测试

```bash
# 构建整个 queue 模块
mvn -pl queue -am clean install

# 仅运行 core 模块单元测试
mvn -pl queue/core test

# 仅运行 starter 模块单元测试
mvn -pl queue/starter test
```
