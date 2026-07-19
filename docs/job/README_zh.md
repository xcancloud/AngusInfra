# 分布式任务调度（angus-infra-job）

[English](README.md) | [中文](README_zh.md)

基于数据库的分布式任务调度框架（非 Quartz / XXL-JOB）：支持 cron 调度、多节点互斥、分片并行、
MapReduce、执行日志与可选运维 REST。

> **不适合：** 秒级/亚秒级高频触发（扫描间隔默认 1s）；需要独立调度中心 UI 的超大集群
> （请评估 XXL-JOB 等）。

---

## 目录

1. [概述](#1-概述)
2. [架构](#2-架构)
3. [数据模型](#3-数据模型)
4. [配置项](#4-配置项)
5. [接入指南](#5-接入指南)
6. [API 参考](#6-api-参考)
7. [注册 / 锁 / 分片 / 执行](#7-注册--锁--分片--执行)
8. [管理 REST](#8-管理-rest)
9. [最佳实践](#9-最佳实践)
10. [排查指南](#10-排查指南)

---

## 1. 概述

| 组件 | 职责 |
|------|------|
| `JobExecutor` / `ShardingJobExecutor` / `MapReduceJobExecutor` | 业务 SPI |
| `@JobDefinition` + `JobRegistrar` | 启动幂等注册 |
| `JobSchedulerService` | 扫描 READY 并执行 |
| `DistributedLockService` | DB 分布式锁 |
| `JobManagementService` | 创建/暂停/触发/统计 |
| `JobHealthMonitor` | 超时告警、清理过期锁 |
| `job-web` | 可选管理 REST |

**设计原则**

1. **DB 驱动调度** — `angus_scheduled_job.next_execute_time` 驱动触发。
2. **DB 分布式锁** — `angus_distributed_lock`，先删过期再插入。
3. **SPI 执行器** — 按 Spring bean 名绑定，注册表白名单防任意反射。
4. **注解幂等注册** — 已存在则跳过，不覆盖运维改过的 cron。
5. **稳定 nodeId** — `hostname|ip`，重启清理本节点遗留锁与 RUNNING。

---

## 2. 架构

```
@JobDefinition JobExecutor
        ↓ JobRegistrar (ApplicationRunner)
angus_scheduled_job
        ↓ JobSchedulerService @Scheduled scan
tryLock(job_lock_{id}) → RUNNING → SIMPLE | SHARDING | MAP_REDUCE
        ↓ success → next cron + READY
        ↓ failure → retry backoff / FAILED
```

---

## 3. 数据模型

脚本：`classpath:schema/mysql/job-schema.sql`（Postgres 同目录）  
**表名带 `angus_` 前缀。**

| 表 | 说明 |
|----|------|
| `angus_scheduled_job` | 任务定义与调度状态；UK `(job_name, job_group)` |
| `angus_job_execution_log` | 执行历史 |
| `angus_job_shard` | 当前轮次分片状态（非长期历史） |
| `angus_distributed_lock` | 分布式锁 |

`angus_scheduled_job` 关键列：`cron_expression`、`bean_name`、`job_type`、`status`、
`sharding_count`、`sharding_parameter`、`retry_count`、`max_retry_count`、
`log_retention_days`、`next_execute_time` 等。

---

## 4. 配置项

前缀：`angus.job`（**无** `enabled` 开关）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `scan-interval-ms` | `1000` | 扫描间隔 |
| `lock-timeout-seconds` | `300` | 锁 TTL；分片/Map 等待也用此值 |
| `executor-core-pool-size` | `10` | 执行池 core |
| `executor-max-pool-size` | `50` | 执行池 max |
| `executor-queue-capacity` | `1000` | 执行池队列 |
| `scheduler-pool-size` | `5` | `@Scheduled` 线程池 |
| `retry-backoff-minutes` | `5` | 失败重试延迟（分钟） |
| `timeout-threshold-minutes` | `30` | 监控超时阈值（不杀线程） |
| `max-jobs-per-scan` | `100` | 单次扫描上限 |

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
  datasource:
    extra:
      entityPackages:
        - cloud.xcan.angus.job.entity
    mysql:
      schema:
        - schema/mysql/job-schema.sql
```

仓库包建议扫描：`cloud.xcan.angus.job.jpa`。

---

## 5. 接入指南

### 5.1 依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.job-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

需要管理台时再加 `xcan-angusinfra.job-web`。

若应用主类不在 `cloud.xcan.angus` 下，需 `@ComponentScan("cloud.xcan.angus.job")`，
否则调度服务进不了容器。

### 5.2 SIMPLE 任务

```java
@Component("dailyReportJob")
@JobDefinition(
    name = "daily-report",
    group = "report",
    cron = "0 0 2 * * *",   // Spring 6 段 cron，非 Quartz 7 段
    maxRetryCount = 3,
    initialDelaySeconds = 10,
    logRetentionDays = 30,
    description = "每日凌晨生成日报"
)
public class DailyReportJob implements JobExecutor {
  @Override
  public JobExecutionResult execute(JobContext context) {
    return JobExecutionResult.builder().success(true).result("ok").build();
  }
}
```

`beanName` 必须等于 Spring bean 名（上例为 `dailyReportJob`）。

### 5.3 SHARDING

```java
@Component("shardSyncJob")
@JobDefinition(
    name = "shard-sync",
    group = "sync",
    cron = "0 */10 * * * *",
    type = JobType.SHARDING,
    shardingCount = 4,
    shardingParameter = "a,b,c,d"
)
public class ShardSyncJob implements ShardingJobExecutor {
  @Override
  public JobExecutionResult executeSharding(JobContext ctx, int item, String param) {
    return JobExecutionResult.builder().success(true).result(param).build();
  }
}
```

### 5.4 MAP_REDUCE

实现 `MapReduceJobExecutor`：`map` 返回 `List<String>`；框架按 shard 把每批 map 结果
`String.join(",", ...)` 成**一条**字符串，再组成 `List<String>` 交给 `reduce`
（每个元素对应一个 shard）。

### 5.5 程序化管理

注入 `JobManagementService`：`createJob` / `pauseJob` / `resumeJob` / `triggerJob` 等。

业务也可复用 `DistributedLockService.tryLock/unlock`。

---

## 6. API 参考

### `@JobDefinition`

| 属性 | 默认 | 说明 |
|------|------|------|
| `name` | 必填 | 与 group 联合唯一 |
| `group` | `default` | 建议用应用短码 |
| `cron` | 必填 | Spring 6 段 |
| `type` | `SIMPLE` | `SIMPLE` / `SHARDING` / `MAP_REDUCE` |
| `shardingCount` | `1` | |
| `shardingParameter` | `""` | 逗号分隔 |
| `maxRetryCount` | `3` | `0`=不重试 |
| `initialDelaySeconds` | `0` | 首次推迟 |
| `logRetentionDays` | `0` | `0`→全局默认 7 天；`-1`→永久 |

### SPI

- `JobExecutor.execute(JobContext)`
- `ShardingJobExecutor.executeSharding(...)`
- `MapReduceJobExecutor.map(...)` / `reduce(...)`
- `JobExecutorRegistry.getExecutor(beanName)`

### `JobManagementService`

`createJob` / `updateJob` / `listJobs` / `getJob` / `deleteJob` /
`pauseJob` / `resumeJob` / `triggerJob` / `getJobExecutionHistory` / `getJobStatistics`

---

## 7. 注册 / 锁 / 分片 / 执行

### 注册（`JobRegistrar`）

启动时：

1. 删除本节点全部锁
2. 本节点遗留 RUNNING → READY
3. 扫描带 `@JobDefinition` 的 `JobExecutor`；已存在 skip；否则 create

改 cron 请用 REST `PUT`，或删库记录后重启。

### 锁

Key：`job_lock_{jobId}`；delete-expired-then-insert；健康监控每 60s 清理过期锁。

### 分片 / MapReduce

每轮先 `deleteByJobId` 再建 shard；任一 shard 失败 → 整 job 失败/重试；
Map 等待超时 = `lock-timeout-seconds`；reduce 日志 `sharding_item=-1`。

### 重试

未达 `maxRetryCount` → READY + `now + retryBackoffMinutes`；否则 FAILED。  
**注意：** `trigger` 不接受 FAILED；`resume` 只接受 PAUSED → **FAILED 目前无正式 API 恢复**
（需改库或删重建）。

---

## 8. 管理 REST

模块：`job-web`，前缀 `/api/v1/jobs`  
鉴权：`@PPS.isCloudTenantSecurity() && @PPS.isSysAdmin()`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/` | 创建 |
| GET | `/` | 分页列表 |
| GET / PUT / DELETE | `/{jobId}` | 详情 / 更新 / 删除 |
| POST | `/{jobId}/pause\|resume\|trigger` | 运维 |
| GET | `/{jobId}/executions` | 历史 |
| GET | `/{jobId}/stats` | 统计 |

`UpdateJobRequest` 可改 name/cron/description/`logRetentionDays`，**不可改**
status / beanName / jobType。

---

## 9. 最佳实践

1. Cron 使用 Spring 6 段表达式。
2. `beanName` 与 `@Component` 名严格一致。
3. `lock-timeout-seconds` 必须大于最坏执行时间。
4. 勿盲目增大 `max-jobs-per-scan`。
5. 容器环境确保 `hostname|ip` 唯一，避免 nodeId 冲突。
6. 内置清理 Job（`jobExecutionLogCleanupJob`，group=`infra`）勿删。
7. MapReduce 按「每 shard 一条汇总字符串」理解 reduce 入参。

---

## 10. 排查指南

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 任务未注册 | 无注解 / 代理未穿透 | 查 `@JobDefinition` 与 bean |
| No JobExecutor registered | bean 名不匹配 | 对齐 `@Component` 名 |
| 任务不触发 | 状态非 READY / next 时间未到 | 查表与 cron |
| 多节点重复执行 | 锁超时过短 | 加大 `lock-timeout-seconds` |
| FAILED 无法恢复 | API 缺口 | 改库 status 或删后重建 |
| 调度服务缺失 | 未扫到 job 包 | `@ComponentScan("cloud.xcan.angus.job")` |
| REST 404 | 未引入 job-web | 加依赖 |

---

## 相关类

| 类 | 模块 |
|----|------|
| `cloud.xcan.angus.job.executor.JobExecutor` | `job-core` |
| `cloud.xcan.angus.job.annotation.JobDefinition` | `job-core` |
| `cloud.xcan.angus.job.autoconfigure.JobAutoConfiguration` | `job-starter` |
| `cloud.xcan.angus.job.service.JobSchedulerService` | `job-starter` |
| `cloud.xcan.angus.job.web.JobController` | `job-web` |
