# AngusInfra — Job Module

[English](README.md) | [中文](README_zh.md)

> The AngusInfra Job module is a database-driven distributed job scheduling framework for Spring
> Boot applications. It provides job definition, distributed locking, parallel sharding, MapReduce
> execution, execution logging, and a REST management API.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Core Components](#3-core-components)
4. [Database Schema](#4-database-schema)
5. [Configuration Reference](#5-configuration-reference)
6. [Integration Guide](#6-integration-guide)
7. [REST Management API](#7-rest-management-api)
8. [Notes](#8-notes)

---

## 1. Overview

The job module consists of two sub-modules:

- `xcan-angusinfra.job-core` — Defines the job SPI, entities, enums, request models, and
  configuration model.
- `xcan-angusinfra.job-starter` — Provides Spring Boot auto-configuration, the scheduler,
  distributed lock service, health monitor, JPA repositories, and REST management endpoints.

Three job types are supported:

| Type          | Enum         | Description                                               |
|---------------|--------------|-----------------------------------------------------------|
| Simple job    | `SIMPLE`     | Single executor runs sequentially once                    |
| Sharding job  | `SHARDING`   | Splits one job into multiple shards executed in parallel  |
| MapReduce job | `MAP_REDUCE` | Parallel map phase, then a single-node reduce aggregation |

Core capabilities:

- Job definition and cron scheduling via the `scheduled_job` database table.
- Multi-node mutual exclusion via the `distributed_lock` table.
- Concurrent job dispatch using a shared thread pool.
- Execution auditing and statistics via `job_execution_log`.
- Shard state tracking and map-phase intermediate result storage via `job_shard`.
- Pause, resume, immediate trigger, delete, execution history, and statistics.
- Built-in expired lock cleanup, timeout job detection, and hourly health reports.

---

## 2. Architecture

### 2.1 Module Layers

```text
Business Application
  ├─ Custom JobExecutor / ShardingJobExecutor / MapReduceJobExecutor
  ├─ Manage jobs via REST API
  └─ Import xcan-angusinfra.job-starter

starter
  ├─ JobAutoConfiguration
  ├─ JobSchedulerService
  ├─ JobManagementService
  ├─ DistributedLockService
  ├─ JobHealthMonitor
  ├─ JPA Repositories
  └─ JobController

core
  ├─ ScheduledJob / JobExecutionLog / JobShard / DistributedLock
  ├─ JobExecutor SPI
  ├─ CreateJobRequest / UpdateJobRequest / JobContext / JobExecutionResult
  └─ JobProperties / enums

Database
  ├─ scheduled_job
  ├─ job_execution_log
  ├─ job_shard
  └─ distributed_lock
```

### 2.2 Scheduling Execution Flow

```text
@Scheduled scanAndExecuteJobs()
    ↓
Query scheduled_job WHERE status=READY AND next_execute_time<=now
    ↓
Attempt to acquire distributed_lock by jobId
    ↓
On lock acquisition: update job status to RUNNING
    ↓
Dispatch by jobType: SIMPLE / SHARDING / MAP_REDUCE
    ↓
Record job_execution_log / job_shard
    ↓
Success: compute next execution time from cron, restore status to READY
Failure: retry_count+1, delay by retry-backoff-minutes; mark FAILED when max_retry_count exceeded
    ↓
Release distributed_lock
```

### 2.3 Distributed Lock Model

`DistributedLockService` uses the `distributed_lock` table for mutual exclusion:

1. Delete any expired lock records for the target `lock_key`.
2. Insert a new lock record.
3. `lock_key` is the primary key — only one node succeeds when multiple nodes insert concurrently.
4. On release, both `owner` and `lockValue` are validated to prevent accidentally removing another
   node's lock.

This design eliminates the TOCTOU race window of a check-then-insert approach, ensuring only one
node executes a given job in a multi-instance deployment.

### 2.4 Three Execution Models

#### SIMPLE

- Executed via `JobExecutor#execute(JobContext)`.
- Each execution produces one `job_execution_log` record.
- Suitable for serial tasks such as daily report generation, cache refresh, and single-table
  cleanup.

#### SHARDING

- The scheduler creates multiple `job_shard` records with count `shardingCount`.
- Each shard calls `ShardingJobExecutor#executeSharding(...)` in parallel on the thread pool.
- Each shard has its own `job_execution_log` record.
- Any shard failure causes the entire job to enter the retry/failure flow.

#### MAP_REDUCE

- Creates shards and runs `MapReduceJobExecutor#map(...)` in parallel.
- Each shard's map result is stored in `job_shard.map_result`.
- After all map shards complete, a single node calls `reduce(...)` to aggregate results.
- The reduce phase uses an execution log entry with `sharding_item=-1` as the sentinel record.

---

## 3. Core Components

### 3.1 Auto-Configuration

`JobAutoConfiguration` is responsible for:

- Enabling `@EnableScheduling`.
- Registering the shared task thread pool `jobExecutorPool` (rejection policy: `CallerRunsPolicy`).
- Registering the Spring scheduler thread pool `taskScheduler`.
- Binding `JobProperties` configuration.

### 3.2 Executor SPI

| Interface              | Purpose                                                     |
|------------------------|-------------------------------------------------------------|
| `JobExecutor`          | Entry point for simple jobs — defines `execute(JobContext)` |
| `ShardingJobExecutor`  | Sharding job interface — defines `executeSharding(...)`     |
| `MapReduceJobExecutor` | MapReduce interface — defines `map(...)` and `reduce(...)`  |
| `JobExecutorRegistry`  | Executor registry — looks up executors by Spring bean name  |

`DefaultJobExecutorRegistry` collects all `JobExecutor` Spring beans into an
immutable `Map<String, JobExecutor>`. The `beanName` in the job definition must exactly match the
Spring bean name.

### 3.3 Job Model

#### ScheduledJob

The job definition entity. Key fields:

- `jobName` / `jobGroup`: Job name and group, combined unique key.
- `cronExpression`: 6-part Spring cron expression.
- `beanName`: Executor bean name.
- `jobType`: `SIMPLE` / `SHARDING` / `MAP_REDUCE`.
- `status`: `READY` / `RUNNING` / `PAUSED` / `COMPLETED` / `FAILED`.
- `shardingCount` / `shardingParameter`: Sharding configuration.
- `retryCount` / `maxRetryCount`: Retry configuration.
- `nextExecuteTime`: Next trigger time.

#### JobExecutionLog

Execution log entity. Key fields:

- `status`: `RUNNING` / `SUCCESS` / `FAILURE` / `TIMEOUT`.
- `executionTime`: Duration in milliseconds.
- `result` / `errorMessage`: Execution result and error info.
- `executorNode`: Node identifier.

#### JobShard

Shard entity. Key fields:

- `shardingItem`: Shard index.
- `shardingParameter`: Parameter for this shard.
- `status`: `PENDING` / `RUNNING` / `COMPLETED` / `FAILED`.
- `mapResult`: Map-phase intermediate result for MapReduce jobs.

### 3.4 Management Service

`JobManagementService` provides:

- `createJob`
- `listJobs` / `getJob`
- `updateJob`
- `pauseJob` / `resumeJob`
- `triggerJob`
- `deleteJob`
- `getJobExecutionHistory`
- `getJobStatistics`

### 3.5 Health Monitor

`JobHealthMonitor` provides three background self-healing tasks:

- Every 60 seconds: clean up expired distributed locks.
- Every 30 seconds: detect execution records that have timed out.
- Every hour: output a success-rate health report for the past hour.

---

## 4. Database Schema

The module uses four tables. Initialization scripts are centralized in core:

- MySQL: `job/core/src/main/resources/schema/mysql/job-schema.sql`
- PostgreSQL: `job/core/src/main/resources/schema/postgres/job-schema.sql`

### 4.1 scheduled_job

Job definition table.

Key fields:

- `job_name` + `job_group`: Combined unique constraint.
- `cron_expression`: Cron schedule expression.
- `bean_name`: Executor bean name.
- `job_type` / `status`: Job type and status.
- `next_execute_time`: Hot query field for the scheduler.

Key indexes:

- `uk_job_name_group(job_name, job_group)`
- `idx_sj_status_next_exec(status, next_execute_time)`

### 4.2 job_execution_log

Execution history table.

Purpose:

- Records each job execution result.
- Sharding jobs create one record per shard.
- The reduce phase in MapReduce uses a separate log entry.

Key indexes:

- `idx_jel_job_id(job_id)`
- `idx_jel_start_time(start_time)`
- `idx_jel_status(status)`

### 4.3 distributed_lock

Distributed lock table.

Key fields:

- `lock_key`: Primary key; typically `job_lock_{jobId}` for each job.
- `lock_value`: UUID generated on each lock acquisition.
- `owner`: Node identifier.
- `expire_time`: Lock expiry time.

Key indexes:

- `idx_dl_expire_time(expire_time)`

### 4.4 job_shard

Job shard table.

Purpose:

- Records shard status for the current execution round.
- Stores map-phase intermediate results.
- Historical shards are deleted and recreated before each new execution round.

Key indexes:

- `idx_js_job_id(job_id)`
- `idx_js_status(status)`

---

## 5. Configuration Reference

Configuration prefix: `angus.job`.

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

| Property                    | Default | Description                                                          |
|-----------------------------|---------|----------------------------------------------------------------------|
| `scan-interval-ms`          | `1000`  | Fixed interval (ms) for scanning due jobs                            |
| `lock-timeout-seconds`      | `300`   | Distributed lock timeout; should exceed most job execution durations |
| `executor-core-pool-size`   | `10`    | Executor thread pool core size                                       |
| `executor-max-pool-size`    | `50`    | Executor thread pool maximum size                                    |
| `executor-queue-capacity`   | `1000`  | Executor thread pool queue capacity                                  |
| `scheduler-pool-size`       | `5`     | Spring `@Scheduled` scheduler thread count                           |
| `retry-backoff-minutes`     | `5`     | Delay in minutes before retrying a failed job                        |
| `timeout-threshold-minutes` | `30`    | Timeout threshold for health monitor detection                       |
| `max-jobs-per-scan`         | `100`   | Maximum jobs loaded per scan cycle (prevents overload)               |

---

## 6. Integration Guide

### 6.1 Add the Dependency

For a standard Spring Boot application, add the starter:

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.job-starter</artifactId>
  <version>3.0.0</version>
</dependency>
```

### 6.2 Initialize the Database Tables

Run the schema script:

- MySQL: `job/core/src/main/resources/schema/mysql/job-schema.sql`
- PostgreSQL: `job/core/src/main/resources/schema/postgres/job-schema.sql`

Or use Spring SQL initialization:

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/mysql/job-schema.sql
```

For PostgreSQL, switch to:

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/postgres/job-schema.sql
```

### 6.3 Enable Configuration

```yaml
angus:
  job:
    scan-interval-ms: 1000
    lock-timeout-seconds: 300
    executor-core-pool-size: 10
    executor-max-pool-size: 50
```

> The module is loaded via `AutoConfiguration.imports`; you do **not** need to
> add `@EnableScheduling` manually.

### 6.4 Scenario 1 — Simple Job (SIMPLE)

Define an executor:

```java
import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.model.JobContext;
import cloud.xcan.angus.job.model.JobExecutionResult;
import org.springframework.stereotype.Component;

@Component("dailyReportJob")
public class DailyReportJob implements JobExecutor {

    @Override
    public JobExecutionResult execute(JobContext context) {
        // Business logic here
        return JobExecutionResult.builder()
            .success(true)
            .result("Daily report generated")
            .build();
    }
}
```

Create job request example:

```json
{
  "jobName": "daily-report",
  "jobGroup": "report",
  "cronExpression": "0 0 2 * * *",
  "beanName": "dailyReportJob",
  "jobType": "SIMPLE",
  "maxRetryCount": 3,
  "description": "Generate daily report at 2 AM"
}
```

### 6.5 Scenario 2 — Sharding Job (SHARDING)

Suitable for batch scanning, partitioned table inspection, sharded synchronization, and other
naturally parallelizable tasks.

```java
import cloud.xcan.angus.job.executor.ShardingJobExecutor;
import cloud.xcan.angus.job.model.JobContext;
import cloud.xcan.angus.job.model.JobExecutionResult;
import org.springframework.stereotype.Component;

@Component("userSyncShardingJob")
public class UserSyncShardingJob implements ShardingJobExecutor {

    @Override
    public JobExecutionResult execute(JobContext context) {
        return null;
    }

    @Override
    public JobExecutionResult executeSharding(JobContext context, int shardingItem,
            String shardingParameter) {
        // Process the specified shard — e.g., a database partition, tenant, or region
        return JobExecutionResult.builder()
            .success(true)
            .result("Shard " + shardingItem + " completed")
            .build();
    }
}
```

Create job request example:

```json
{
  "jobName": "user-sync",
  "jobGroup": "sync",
  "cronExpression": "0 */10 * * * *",
  "beanName": "userSyncShardingJob",
  "jobType": "SHARDING",
  "shardingCount": 4,
  "shardingParameter": "p0,p1,p2,p3",
  "maxRetryCount": 2,
  "description": "Sync users every 10 minutes"
}
```

### 6.6 Scenario 3 — MapReduce Job (MAP_REDUCE)

Suitable for parallel computation followed by aggregation — e.g., log aggregation, statistics
rollup, batch scoring.

```java
import cloud.xcan.angus.job.executor.MapReduceJobExecutor;
import cloud.xcan.angus.job.model.JobContext;
import cloud.xcan.angus.job.model.JobExecutionResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("salesSummaryJob")
public class SalesSummaryJob implements MapReduceJobExecutor {

    @Override
    public JobExecutionResult execute(JobContext context) {
        return null;
    }

    @Override
    public List<String> map(JobContext context, int shardingItem, String shardingParameter) {
        // Each shard computes its own intermediate result
        return List.of("100", "200");
    }

    @Override
    public String reduce(JobContext context, List<String> mapResults) {
        int total = mapResults.stream().mapToInt(Integer::parseInt).sum();
        return "sum=" + total;
    }
}
```

Create job request example:

```json
{
  "jobName": "sales-summary",
  "jobGroup": "analytics",
  "cronExpression": "0 0/30 * * * *",
  "beanName": "salesSummaryJob",
  "jobType": "MAP_REDUCE",
  "shardingCount": 8,
  "shardingParameter": "s0,s1,s2,s3,s4,s5,s6,s7",
  "maxRetryCount": 1,
  "description": "Summarize sales data every 30 minutes"
}
```

### 6.7 Scenario 4 — Manage Jobs via REST API

Controller prefix: `/api/v1/jobs`

| Method   | Path                              | Description                        |
|----------|-----------------------------------|------------------------------------|
| `POST`   | `/api/v1/jobs`                    | Create a job                       |
| `GET`    | `/api/v1/jobs`                    | Paginated job list                 |
| `GET`    | `/api/v1/jobs/{jobId}`            | Get job detail                     |
| `PUT`    | `/api/v1/jobs/{jobId}`            | Update job name, cron, description |
| `DELETE` | `/api/v1/jobs/{jobId}`            | Delete a job and its shards/logs   |
| `POST`   | `/api/v1/jobs/{jobId}/pause`      | Pause a job                        |
| `POST`   | `/api/v1/jobs/{jobId}/resume`     | Resume a job                       |
| `POST`   | `/api/v1/jobs/{jobId}/trigger`    | Trigger a job immediately          |
| `GET`    | `/api/v1/jobs/{jobId}/executions` | Execution history                  |
| `GET`    | `/api/v1/jobs/{jobId}/statistics` | Job statistics                     |

Statistics response fields include:

- `totalExecutions`
- `successCount`
- `failureCount`
- `successRate`
- `avgExecutionTime`

### 6.8 Scenario 5 — Operations and Monitoring

Built-in operational capabilities:

- Lock cleanup: expired locks removed every minute.
- Timeout detection: stale `RUNNING` records detected every 30 seconds.
- Health report: hourly success-rate report logged.

Recommended log entries to monitor:

- `Executing job:`
- `Job execution failed:`
- `Job ... will be retried`
- `Cleaned ... expired distributed lock(s)`
- `Found ... job execution(s) potentially timed out`

---

## 7. REST Management API

### 7.1 CreateJobRequest

Field constraints:

- `jobName`: Required, max length 255.
- `jobGroup`: Required, max length 255.
- `cronExpression`: Required, 6-part cron.
- `beanName`: Required; only letters, digits, underscores, and hyphens allowed.
- `jobType`: Required.
- `shardingCount`: Minimum value 1.
- `shardingParameter`: Max length 4000.
- `maxRetryCount`: Minimum value 0.
- `description`: Max length 4000.

### 7.2 UpdateJobRequest

Only the following fields may be updated:

- `jobName`
- `cronExpression`
- `description`

Runtime state fields (`status`, `lastExecuteTime`, `nextExecuteTime`, `retryCount`, etc.) are
managed by the framework and cannot be directly modified via the update API.

---

## 8. Notes

1. `beanName` must exactly match the Spring bean name of the executor. If the bean cannot be found,
   a "No JobExecutor registered" exception will be thrown at execution time.
2. `cronExpression` uses the Spring 6-part cron parser — not Quartz's 7-part extended format.
3. `trigger` only applies to jobs in `READY` or `PAUSED` status. Jobs in `RUNNING` or `FAILED`
   status cannot be triggered directly.
4. Both sharding and MapReduce jobs delete previous-round shards before creating new ones.
   The `job_shard` table reflects the current/most-recent execution round, not a permanent history.
5. Any shard failure causes the entire `SHARDING` or `MAP_REDUCE` job to enter its failure handling
   flow.
6. If `lock-timeout-seconds` is set too low, a long-running job may have its lock re-acquired by
   another node before completion. Set this conservatively based on maximum expected execution time.
7. `max-jobs-per-scan` prevents a backlog of overdue jobs from flooding the thread pool at once. Do
   not blindly increase this for high-volume scenarios.
8. `ExecutionStatus.TIMEOUT` is primarily a monitoring semantic. The health monitor detects
   long-running `RUNNING` records and logs a warning, but does not forcibly interrupt the business
   thread.
