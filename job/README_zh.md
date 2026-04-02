Job 模块
==========================

> 说明：AngusInfra Job 是一个基于数据库驱动的分布式任务调度模块，面向 Spring Boot
> 应用提供任务定义、分布式锁、并行分片、MapReduce、执行日志和运维管理接口。

---

## 目录

1. 功能概述
2. 架构设计
3. 核心组件
4. 数据表设计
5. 配置参考
6. 三方接入说明
7. REST 管理接口
8. 注意事项

---

## 1. 功能概述

job 模块由 `core` 与 `starter` 两部分组成：

- `xcan-angusinfra.job-core`：定义任务 SPI、实体、枚举、请求模型、配置模型和 `@JobDefinition` 注解。
- `xcan-angusinfra.job-starter`：提供 Spring Boot 自动装配、调度器、分布式锁服务、健康监控、JPA
  Repository、REST 管理接口和 `JobRegistrar`（启动时自动注册）。

模块当前支持三种任务类型：

| 类型           | 枚举           | 说明                     |
|--------------|--------------|------------------------|
| 简单任务         | `SIMPLE`     | 单执行器串行执行一次             |
| 分片任务         | `SHARDING`   | 同一任务拆成多个 shard 并行执行    |
| MapReduce 任务 | `MAP_REDUCE` | 先并行 map，再单节点 reduce 汇总 |

核心能力包括：

- 基于数据库表 `scheduled_job` 的任务定义与 cron 调度。
- 基于表 `distributed_lock` 的多节点互斥执行。
- 基于共享线程池的并发任务派发。
- 基于 `job_execution_log` 的执行审计与统计。
- 基于 `job_shard` 的分片态跟踪与 Map 阶段中间结果保存。
- 支持暂停、恢复、立即触发、删除、查看历史和统计。
- 内置过期锁清理、超时任务检测与小时级健康报告。
- **`@JobDefinition` 注解驱动的自动注册**：应用启动时幂等写入 `scheduled_job`，无需手工 SQL。

---

## 2. 架构设计

### 2.1 模块分层

```text
业务应用
  ├─ 自定义 JobExecutor / ShardingJobExecutor / MapReduceJobExecutor
  │     └─ 标注 @JobDefinition（声明 cron、group、重试策略等）
  ├─ 通过 REST API 管理任务
  └─ 引入 xcan-angusinfra.job-starter

starter
  ├─ JobAutoConfiguration
  ├─ JobRegistrar          ← 启动时扫描 @JobDefinition，幂等注册到 scheduled_job
  ├─ JobSchedulerService
  ├─ JobManagementService
  ├─ DistributedLockService
  ├─ JobHealthMonitor
  ├─ JPA Repositories
  └─ JobController

core
  ├─ ScheduledJob / JobExecutionLog / JobShard / DistributedLock
  ├─ JobExecutor SPI
  ├─ @JobDefinition 注解
  ├─ CreateJobRequest / UpdateJobRequest / JobContext / JobExecutionResult
  └─ JobProperties / enums

数据库
  ├─ scheduled_job
  ├─ job_execution_log
  ├─ job_shard
  └─ distributed_lock
```

### 2.2 调度执行流程

```text
@Scheduled scanAndExecuteJobs()
    ↓
查询 scheduled_job 中 status=READY 且 next_execute_time<=now 的任务
    ↓
按 jobId 尝试获取 distributed_lock
    ↓
抢锁成功后将任务状态更新为 RUNNING
    ↓
根据 jobType 分派：SIMPLE / SHARDING / MAP_REDUCE
    ↓
记录 job_execution_log / job_shard
    ↓
成功：按 cron 计算下次执行时间，状态恢复为 READY
失败：retry_count+1，按 retry-backoff-minutes 延迟重试；超过 max_retry_count 则标记 FAILED
    ↓
释放 distributed_lock
```

### 2.3 自动注册流程（JobRegistrar）

```text
应用启动 → ApplicationRunner.run()
    ↓
遍历 Spring 容器中所有 JobExecutor Bean
    ↓
读取 Bean 上的 @JobDefinition 注解（自动穿透 CGLIB/JDK 代理）
    ↓
findByJobNameAndJobGroup 检查是否已存在
  ├─ 已存在 → 跳过（幂等，保护运维人员通过 REST API 做的修改）
  └─ 不存在 → 调用 JobManagementService.createJob() 写入数据库
    ↓
若 initialDelaySeconds > 0，将 next_execute_time 推迟相应秒数
    ↓
输出汇总日志：注册 N 个，跳过 M 个，失败 K 个
```

### 2.4 分布式锁模型

`DistributedLockService` 使用数据库表 `distributed_lock` 实现互斥：

- 先删除当前 `lock_key` 下已过期的锁记录。
- 再插入新锁记录。
- `lock_key` 是主键，多个节点并发插入时只有一个节点成功。
- 解锁时同时校验 `owner` 和 `lockValue`，避免误删其他节点锁。

该设计避免了"先查再删再插"的 TOCTOU 竞争窗口，满足多实例部署下同一任务只会被一个节点执行。

### 2.5 三种任务执行模型

#### SIMPLE

- 通过 `JobExecutor#execute(JobContext)` 执行。
- 单次执行对应一条 `job_execution_log`。
- 适合日报生成、缓存刷新、单表清理等串行任务。

#### SHARDING

- 调度器先按 `shardingCount` 创建多条 `job_shard`。
- 每个 shard 在线程池中并行调用 `ShardingJobExecutor#executeSharding(...)`。
- 每个 shard 都有独立的 `job_execution_log` 记录。
- 任一 shard 失败，整个任务视为失败并进入重试/失败流程。

#### MAP_REDUCE

- 先创建 shard 并并行执行 `MapReduceJobExecutor#map(...)`。
- 每个 shard 的 map 结果会落到 `job_shard.map_result`。
- map 全部结束后，由单节点执行 `reduce(...)` 汇总。
- reduce 阶段使用 `sharding_item=-1` 的执行日志作为哨兵记录。

---

## 3. 核心组件

### 3.1 自动装配

`JobAutoConfiguration` 负责：

- 开启 `@EnableScheduling`。
- 注册共享任务线程池 `jobExecutorPool`。
- 注册 Spring 调度线程池 `taskScheduler`。
- 启用 `JobProperties` 配置绑定。
- 注册 `JobRegistrar`（`@ConditionalOnMissingBean`，可覆盖）。

默认线程池策略：

- 任务执行池拒绝策略使用 `CallerRunsPolicy`。
- 调度线程池用于触发 `@Scheduled` 扫描和监控任务。

### 3.2 执行器 SPI

| 接口                     | 作用                                         |
|------------------------|--------------------------------------------|
| `JobExecutor`          | 普通任务统一入口，定义 `execute(JobContext)`          |
| `ShardingJobExecutor`  | 分片任务接口，定义 `executeSharding(...)`           |
| `MapReduceJobExecutor` | MapReduce 接口，定义 `map(...)` 与 `reduce(...)` |
| `JobExecutorRegistry`  | 执行器注册表，按 Spring Bean 名获取执行器                |

`DefaultJobExecutorRegistry` 会将所有实现 `JobExecutor` 的 Spring Bean
收集到不可变 `Map<String, JobExecutor>` 中，因此任务定义里的 `beanName` 必须与 Spring Bean 名完全一致。

### 3.3 `@JobDefinition` 注解

声明在 `JobExecutor` 实现类上，由 `JobRegistrar` 在启动时读取并注册。

| 属性                   | 类型        | 默认值       | 说明                                       |
|----------------------|-----------|-----------|------------------------------------------|
| `name`               | `String`  | —（必填）     | 任务名称，与 `group` 联合唯一                      |
| `group`              | `String`  | `default` | 任务分组，建议使用应用短码（如 `gm`、`git`）             |
| `cron`               | `String`  | —（必填）     | 6 段 Spring cron 表达式                      |
| `type`               | `JobType` | `SIMPLE`  | 任务类型                                     |
| `shardingCount`      | `int`     | `1`       | 分片数，仅 SHARDING / MAP_REDUCE 有意义          |
| `shardingParameter`  | `String`  | `""`      | 逗号分隔的分片参数                                |
| `maxRetryCount`      | `int`     | `3`       | 失败最大重试次数，`0` 表示不重试                       |
| `initialDelaySeconds`| `int`     | `0`       | 首次触发延迟秒数，用于让应用充分预热                       |
| `description`        | `String`  | `""`      | 任务描述，写入 `scheduled_job.description`      |

### 3.4 JobRegistrar

`JobRegistrar` 实现 `ApplicationRunner`，在应用启动就绪后执行：

- 遍历所有 `JobExecutor` Bean，通过 `AopUtils.getTargetClass` 穿透代理读取 `@JobDefinition`。
- 按 `name + group` 幂等检查，已存在则跳过。
- 通过 `JobManagementService.createJob()` 写入数据库，同步计算首次触发时间。
- 支持 `@Conditional` 条件 Bean：条件不满足时 Bean 不存在，自动不注册。
- 可通过 `@ConditionalOnMissingBean(JobRegistrar.class)` 提供自定义子类覆盖默认行为。

### 3.5 任务模型

#### ScheduledJob

任务定义实体，关键字段：

- `jobName` / `jobGroup`：任务名称与分组，联合唯一。
- `cronExpression`：6 段式 cron 表达式。
- `beanName`：执行器 Bean 名。
- `jobType`：`SIMPLE` / `SHARDING` / `MAP_REDUCE`。
- `status`：`READY` / `RUNNING` / `PAUSED` / `COMPLETED` / `FAILED`。
- `shardingCount` / `shardingParameter`：分片配置。
- `retryCount` / `maxRetryCount`：失败重试配置。
- `nextExecuteTime`：下次触发时间。

#### JobExecutionLog

执行日志实体，关键字段：

- `status`：`RUNNING` / `SUCCESS` / `FAILURE` / `TIMEOUT`。
- `executionTime`：耗时毫秒数。
- `result` / `errorMessage`：执行结果与错误信息。
- `executorNode`：执行节点标识。

#### JobShard

分片实体，关键字段：

- `shardingItem`：分片序号。
- `shardingParameter`：该分片参数。
- `status`：`PENDING` / `RUNNING` / `COMPLETED` / `FAILED`。
- `mapResult`：MapReduce 的 map 中间结果。

### 3.6 管理服务

`JobManagementService` 提供：

- 创建任务 `createJob`
- 查询任务 `listJobs` / `getJob`
- 更新任务 `updateJob`
- 暂停/恢复 `pauseJob` / `resumeJob`
- 立即触发 `triggerJob`
- 删除任务 `deleteJob`
- 查看执行历史 `getJobExecutionHistory`
- 统计指标 `getJobStatistics`

### 3.7 健康监控

`JobHealthMonitor` 提供三类后台自愈能力：

- 每 60 秒清理过期分布式锁。
- 每 30 秒检测运行超时的执行记录。
- 每小时输出一次最近 1 小时的执行成功率报告。

---

## 4. 数据表设计

模块当前使用四张表，初始化脚本统一放在 core 模块：

- MySQL：`job/core/src/main/resources/schema/mysql/job-schema.sql`
- PostgreSQL：`job/core/src/main/resources/schema/postgres/job-schema.sql`

### 4.1 scheduled_job

任务定义表。

关键字段：

- `job_name` + `job_group`：联合唯一。
- `cron_expression`：调度表达式。
- `bean_name`：执行器 Bean 名。
- `job_type` / `status`：任务类型与状态。
- `next_execute_time`：扫描器热点查询字段。

关键索引：

- `uk_job_name_group(job_name, job_group)`
- `idx_sj_status_next_exec(status, next_execute_time)`

### 4.2 job_execution_log

任务执行日志表。

用途：

- 记录每次任务执行结果。
- 分片任务按 shard 记录。
- MapReduce 的 reduce 阶段用独立日志记录。

关键索引：

- `idx_jel_job_id(job_id)`
- `idx_jel_start_time(start_time)`
- `idx_jel_status(status)`

### 4.3 distributed_lock

分布式锁表。

关键字段：

- `lock_key`：主键，同一任务通常为 `job_lock_{jobId}`。
- `lock_value`：每次抢锁生成的 UUID。
- `owner`：节点标识。
- `expire_time`：锁过期时间。

关键索引：

- `idx_dl_expire_time(expire_time)`

### 4.4 job_shard

任务分片表。

用途：

- 记录当前执行轮次的 shard 状态。
- 记录 Map 阶段中间结果。
- 每次新轮次执行前会先删除历史 shard 并重建。

关键索引：

- `idx_js_job_id(job_id)`
- `idx_js_status(status)`

---

## 5. 配置参考

配置前缀为 `angus.job`。

```yaml
angus:
  job:
    scan-interval-ms: 1000
    lock-timeout-seconds: 300
    executor-core-pool-size: 10
    executor-max-pool-size: 50
    executor-queue-capacity: 1000
    scheduler-pool-size: 5
    retry-backoff-minutes: 5
    timeout-threshold-minutes: 30
    max-jobs-per-scan: 100
```

配置说明：

| 配置项                         | 默认值  | 说明                        |
|-----------------------------|------|---------------------------|
| `scan-interval-ms`          | 1000 | 扫描到期任务的固定间隔               |
| `lock-timeout-seconds`      | 300  | 分布式锁超时时间，建议大于大多数任务执行时长    |
| `executor-core-pool-size`   | 10   | 执行线程池核心线程数                |
| `executor-max-pool-size`    | 50   | 执行线程池最大线程数                |
| `executor-queue-capacity`   | 1000 | 执行线程池队列容量                 |
| `scheduler-pool-size`       | 5    | Spring `@Scheduled` 调度线程数 |
| `retry-backoff-minutes`     | 5    | 失败后的延迟重试分钟数               |
| `timeout-threshold-minutes` | 30   | 监控中判定超时的阈值                |
| `max-jobs-per-scan`         | 100  | 单次扫描最多加载任务数，防止过载          |

---

## 6. 三方接入说明

### 6.1 引入依赖

普通 Spring Boot 应用接入推荐直接引入 starter：

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.job-starter</artifactId>
  <version>3.0.0</version>
</dependency>
```

### 6.2 准备数据库表

执行脚本：

- MySQL：`job/core/src/main/resources/schema/mysql/job-schema.sql`
- PostgreSQL：`job/core/src/main/resources/schema/postgres/job-schema.sql`

如果应用统一使用 Spring SQL 初始化机制，可以这样配置：

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/mysql/job-schema.sql
```

PostgreSQL 场景改为：

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/postgres/job-schema.sql
```

### 6.3 开启配置

```yaml
angus:
  job:
    scan-interval-ms: 1000
    lock-timeout-seconds: 300
    executor-core-pool-size: 10
    executor-max-pool-size: 50
```

> 模块通过 `AutoConfiguration.imports` 自动加载 `JobAutoConfiguration`，无需手工 `@EnableScheduling`。

### 6.4 场景一：注解驱动自动注册（推荐）

这是最简洁的接入方式。在执行器类上同时标注 `@Component` 和 `@JobDefinition`，启动时
`JobRegistrar` 自动将任务写入 `scheduled_job` 表，无需手工 SQL 或调用 REST API。

```java
@Component("dailyReportJob")
@JobDefinition(
    name        = "daily-report",
    group       = "report",
    cron        = "0 0 2 * * *",
    maxRetryCount = 3,
    initialDelaySeconds = 10,
    description = "每日凌晨生成日报"
)
public class DailyReportJob implements JobExecutor {

  @Override
  public JobExecutionResult execute(JobContext context) {
    // 业务逻辑
    return JobExecutionResult.builder()
        .success(true)
        .result("日报生成完成")
        .build();
  }
}
```

**幂等保证**：已存在同名 Job 时自动跳过，重启不会覆盖运维人员通过 REST API 修改过的 cron。

**条件 Bean 支持**：若执行器类上标注了 `@Conditional`（如私有化版本条件），条件不满足时 Bean 不注册，
`JobRegistrar` 自然跳过，无需特殊处理。

### 6.5 场景二：接入普通任务（SIMPLE）

与场景一相同，`@JobDefinition` 默认 `type = SIMPLE`。适合日报生成、缓存刷新、单表清理等串行任务。

### 6.6 场景三：接入分片任务（SHARDING）

适合批量扫描、分库分表巡检、分片同步等天然可并行的任务。

```java
@Component("userSyncShardingJob")
@JobDefinition(
    name           = "user-sync",
    group          = "sync",
    cron           = "0 */10 * * * *",
    type           = JobType.SHARDING,
    shardingCount  = 4,
    shardingParameter = "p0,p1,p2,p3",
    maxRetryCount  = 2,
    description    = "每10分钟执行一次用户同步"
)
public class UserSyncShardingJob implements ShardingJobExecutor {

  @Override
  public JobExecutionResult execute(JobContext context) {
    return null;
  }

  @Override
  public JobExecutionResult executeSharding(JobContext context, int shardingItem,
      String shardingParameter) {
    // 处理指定分片，例如数据库分片、租户分片或分区编号
    return JobExecutionResult.builder()
        .success(true)
        .result("分片 " + shardingItem + " 完成")
        .build();
  }
}
```

### 6.7 场景四：接入 MapReduce 任务（MAP_REDUCE）

适合先并行计算、再统一聚合的场景，例如日志聚合、统计汇总、批量评分。

```java
@Component("salesSummaryJob")
@JobDefinition(
    name          = "sales-summary",
    group         = "analytics",
    cron          = "0 0/30 * * * *",
    type          = JobType.MAP_REDUCE,
    shardingCount = 8,
    shardingParameter = "s0,s1,s2,s3,s4,s5,s6,s7",
    maxRetryCount = 1,
    description   = "半小时汇总一次销售数据"
)
public class SalesSummaryJob implements MapReduceJobExecutor {

  @Override
  public JobExecutionResult execute(JobContext context) {
    return null;
  }

  @Override
  public List<String> map(JobContext context, int shardingItem, String shardingParameter) {
    // 每个分片计算自己的中间结果
    return List.of("100", "200");
  }

  @Override
  public String reduce(JobContext context, List<String> mapResults) {
    int total = mapResults.stream().mapToInt(Integer::parseInt).sum();
    return "sum=" + total;
  }
}
```

### 6.8 场景五：通过 REST API 管理任务

控制器路径前缀：`/api/v1/jobs`

主要接口：

| 方法       | 路径                                | 说明              |
|----------|-----------------------------------|-----------------|
| `POST`   | `/api/v1/jobs`                    | 创建任务            |
| `GET`    | `/api/v1/jobs`                    | 分页查询任务          |
| `GET`    | `/api/v1/jobs/{jobId}`            | 查询任务详情          |
| `PUT`    | `/api/v1/jobs/{jobId}`            | 更新任务名称、cron、描述  |
| `DELETE` | `/api/v1/jobs/{jobId}`            | 删除任务及其 shard/日志 |
| `POST`   | `/api/v1/jobs/{jobId}/pause`      | 暂停任务            |
| `POST`   | `/api/v1/jobs/{jobId}/resume`     | 恢复任务            |
| `POST`   | `/api/v1/jobs/{jobId}/trigger`    | 立即触发一次          |
| `GET`    | `/api/v1/jobs/{jobId}/executions` | 查看执行历史          |
| `GET`    | `/api/v1/jobs/{jobId}/statistics` | 查看统计信息          |

统计接口返回的典型字段：

- `totalExecutions`
- `successCount`
- `failureCount`
- `successRate`
- `avgExecutionTime`

### 6.9 场景六：运维与监控接入

模块已内置基本运维能力：

- 锁清理：每分钟清理一次过期锁。
- 超时检测：每 30 秒扫描执行超时记录。
- 健康报告：每小时输出最近 1 小时成功率。

建议接入日志系统并关注以下关键日志：

- `Executing job:`
- `Job execution failed:`
- `Job ... will be retried`
- `Cleaned ... expired distributed lock(s)`
- `Found ... job execution(s) potentially timed out`
- `Registered job:` / `Job already registered, skipping:`
- `Job registration complete —`

---

## 7. REST 管理接口请求模型

### 7.1 CreateJobRequest

字段约束：

- `jobName`：必填，最长 255。
- `jobGroup`：必填，最长 255。
- `cronExpression`：必填，6 段 cron。
- `beanName`：必填，仅允许字母、数字、下划线、短横线。
- `jobType`：必填。
- `shardingCount`：最小值 1。
- `shardingParameter`：最长 4000。
- `maxRetryCount`：最小值 0。
- `description`：最长 4000。

### 7.2 UpdateJobRequest

当前仅允许更新：

- `jobName`
- `cronExpression`
- `description`

任务状态、上次执行时间、下次执行时间、重试计数等运行态字段由框架维护，不允许通过更新接口直接修改。

---

## 8. 注意事项

1. `beanName` 必须与 Spring 容器中的执行器 Bean 名完全一致，否则执行时会抛出 "No JobExecutor
   registered" 异常。
2. `cronExpression` 使用 Spring 的 6 段表达式解析器，不是 Quartz 的 7 段扩展格式。
3. `trigger` 只能触发 `READY` 或 `PAUSED` 任务，`RUNNING` 和 `FAILED` 状态不能直接触发。
4. 分片任务与 MapReduce 任务都会先删除上一轮遗留 shard 再创建新 shard，因此 `job_shard`
   表表达的是"当前/最近一次运行态"，不是永久历史。
5. 任一 shard 失败会导致整个 SHARDING / MAP_REDUCE 任务进入失败处理流程。
6. 锁超时时间 `lock-timeout-seconds` 过小会造成长任务尚未执行完成就被其他节点重新抢到，生产环境要按最大执行时长保守设置。
7. `max-jobs-per-scan` 用于防止积压任务一次性灌满线程池；大批量任务场景不要盲目调大。
8. `ExecutionStatus.TIMEOUT` 目前主要用于监控语义，健康监控会识别长时间 `RUNNING`
   记录并报警，但不会自动强制中断业务线程。
9. `@JobDefinition` 注册是**幂等的**：重启不会覆盖已有记录。若需强制更新 cron，使用
   `PUT /api/v1/jobs/{id}` REST 接口，或先删除数据库记录再重启。
10. `JobRegistrar` 支持通过 `@ConditionalOnMissingBean(JobRegistrar.class)` 替换为自定义实现，
    以满足特殊注册逻辑（如多数据源、动态 cron 从配置中心读取等）。
