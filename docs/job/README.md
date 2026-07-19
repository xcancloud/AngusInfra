# Distributed Job Scheduler (angus-infra-job)

[English](README.md) | [中文](README_zh.md)

A database-backed distributed job scheduler (not Quartz / XXL-JOB): cron triggers, multi-node
mutex locks, sharding, MapReduce, execution logs, and optional ops REST.

> **Not for:** sub-second high-frequency triggers, or large clusters that need a dedicated
> scheduling console (consider XXL-JOB, etc.).

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Data Model](#3-data-model)
4. [Configuration](#4-configuration)
5. [Integration Guide](#5-integration-guide)
6. [API Reference](#6-api-reference)
7. [Registration / Lock / Sharding / Execution](#7-registration--lock--sharding--execution)
8. [Management REST](#8-management-rest)
9. [Best Practices](#9-best-practices)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Overview

| Piece | Responsibility |
|-------|----------------|
| `JobExecutor` / `ShardingJobExecutor` / `MapReduceJobExecutor` | Business SPI |
| `@JobDefinition` + `JobRegistrar` | Idempotent bootstrap registration |
| `JobSchedulerService` | Scan READY jobs and execute |
| `DistributedLockService` | DB distributed lock |
| `JobManagementService` | CRUD / pause / trigger / stats |
| `JobHealthMonitor` | Timeout warnings, expired lock cleanup |
| `job-web` | Optional management REST |

**Design principles:** DB-driven scheduling; DB locks; SPI executors by bean name (allow-list);
idempotent annotation registration; stable `hostname|ip` nodeId with crash recovery.

---

## 2. Architecture

```
@JobDefinition → JobRegistrar → angus_scheduled_job
JobSchedulerService scans READY → tryLock → RUNNING
  → SIMPLE | SHARDING | MAP_REDUCE
  → success: next cron + READY | failure: retry / FAILED
```

---

## 3. Data Model

Scripts: `classpath:schema/mysql/job-schema.sql` (Postgres sibling).  
Tables use the **`angus_` prefix**:

- `angus_scheduled_job`
- `angus_job_execution_log`
- `angus_job_shard` (current round only)
- `angus_distributed_lock`

---

## 4. Configuration

Prefix: `angus.job` (no `enabled` switch).

| Property | Default | Description |
|----------|---------|-------------|
| `scan-interval-ms` | `1000` | Scan interval |
| `lock-timeout-seconds` | `300` | Lock TTL; also shard/Map wait |
| `executor-core-pool-size` | `10` | Executor core |
| `executor-max-pool-size` | `50` | Executor max |
| `executor-queue-capacity` | `1000` | Queue capacity |
| `scheduler-pool-size` | `5` | `@Scheduled` pool |
| `retry-backoff-minutes` | `5` | Retry delay |
| `timeout-threshold-minutes` | `30` | Monitor threshold (no kill) |
| `max-jobs-per-scan` | `100` | Scan batch cap |

Scan entity package `cloud.xcan.angus.job.entity` and repos under `cloud.xcan.angus.job.jpa`.

---

## 5. Integration Guide

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.job-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

Optional: `xcan-angusinfra.job-web`.  
If the app root is not under `cloud.xcan.angus`, add
`@ComponentScan("cloud.xcan.angus.job")`.

```java
@Component("dailyReportJob")
@JobDefinition(
    name = "daily-report",
    group = "report",
    cron = "0 0 2 * * *", // Spring 6-field cron
    maxRetryCount = 3,
    logRetentionDays = 30
)
public class DailyReportJob implements JobExecutor {
  @Override
  public JobExecutionResult execute(JobContext context) {
    return JobExecutionResult.builder().success(true).result("ok").build();
  }
}
```

`beanName` must match the Spring bean name. Implement `ShardingJobExecutor` /
`MapReduceJobExecutor` for advanced types.

---

## 6. API Reference

`@JobDefinition`: `name`, `group`, `cron`, `type`, `shardingCount`, `shardingParameter`,
`maxRetryCount`, `initialDelaySeconds`, `description`, `logRetentionDays`
(`0` → default 7 days, `-1` → keep forever).

`JobManagementService`: create/update/list/get/delete/pause/resume/trigger/history/stats.

`DistributedLockService`: `tryLock` / `unlock` / `renewLock` (reusable by business code).

---

## 7. Registration / Lock / Sharding / Execution

- Registrar clears local locks + stale RUNNING, then registers missing `@JobDefinition` jobs.
- Existing jobs are **not** overwritten on restart — change cron via REST or delete+restart.
- Lock key: `job_lock_{jobId}`.
- Any shard failure fails the whole job.
- MapReduce: each shard's `List<String>` is joined into **one** string before `reduce`.
- **FAILED recovery gap:** `trigger` rejects FAILED; `resume` only accepts PAUSED — recover via
  DB update or delete+recreate.

---

## 8. Management REST

Module `job-web`, prefix `/api/v1/jobs`, sys-admin `PPS` security.

CRUD + `pause` / `resume` / `trigger` / `executions` / `stats`.  
Update may change name/cron/description/`logRetentionDays` only.

---

## 9. Best Practices

1. Use Spring 6-field cron.
2. Keep bean names aligned with `bean_name`.
3. Set `lock-timeout-seconds` above worst-case duration.
4. Do not raise `max-jobs-per-scan` blindly.
5. Ensure unique `hostname|ip` in containers.
6. Keep the builtin cleanup job (`jobExecutionLogCleanupJob`, group `infra`).

---

## 10. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Not registered | Missing annotation / proxy | Check `@JobDefinition` |
| No JobExecutor registered | Bean name mismatch | Align `@Component` name |
| Never fires | Not READY / next time | Inspect row + cron |
| Duplicate runs | Lock TTL too short | Increase timeout |
| FAILED stuck | API gap | Update DB or recreate |
| Scheduler missing | Package not scanned | Scan `cloud.xcan.angus.job` |

---

## Related classes

| Class | Module |
|-------|--------|
| `cloud.xcan.angus.job.executor.JobExecutor` | `job-core` |
| `cloud.xcan.angus.job.annotation.JobDefinition` | `job-core` |
| `cloud.xcan.angus.job.autoconfigure.JobAutoConfiguration` | `job-starter` |
| `cloud.xcan.angus.job.web.JobController` | `job-web` |
