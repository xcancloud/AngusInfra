# Database & Table Sharding (angus-infra-sharding)

[English](README.md) | [中文](README_zh.md)

A lightweight Spring Boot + Spring Data JPA **sharding** framework driven by ThreadLocal
`ShardContext`: routes datasources and rewrites SQL table names, with on-demand DDL and pluggable
strategies.

> **Two modes:**  
> 1. **Full DB+table sharding** — `@Sharding` + dedicated `shardingEntityManagerFactory`  
> 2. **Embedded table-only** — `@ShardedRepository` + `@ShardedTable` (primary DS, rename tables)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Data Model](#3-data-model)
4. [Configuration](#4-configuration)
5. [Integration Guide](#5-integration-guide)
6. [API Reference](#6-api-reference)
7. [Routing & Naming](#7-routing--naming)
8. [Migration from metricsds](#8-migration-from-metricsds)
9. [Best Practices](#9-best-practices)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Overview

| Piece | Responsibility |
|-------|----------------|
| `ShardingAspect` | Resolve `@Sharding`, set `ShardContext` |
| `ShardingRoutingDataSource` | Pick pool by `dataSourceKey` |
| `ShardingTableInterceptor` | Hibernate `StatementInspector` rewrite |
| `ShardTableManager` | On-demand DDL from templates |
| `ShardTableRegistry` | Track physical tables (memory / JDBC / JPA) |
| `ShardingStrategy` | Compute dbIndex / tableIndex |

**Design principles:** context-driven routing; dual-layer shard (DB + table); SPI extensibility;
on-demand `CREATE TABLE IF NOT EXISTS`; disabled by default (`enabled=false`).

---

## 2. Architecture

```
@Sharding → ShardingAspect → ShardContext
                ↓
ShardingRoutingDataSource → Hibernate SQL → ShardingTableInterceptor
                ↓
        ensureTablesExist + rewrite table name
```

`ShardedRepositoryAspect` is **not** auto-registered — declare a `@Bean` for embedded mode.

---

## 3. Data Model

Optional registry table `angus_shard_table` (`table_name`, `shard_key`, `db_index`, `table_index`).

Business DDL templates: `classpath:{schema-path}/{template}.sql` (default path `sharding/schema`).

---

## 4. Configuration

Prefix: `angus.sharding`

| Property | Default | Description |
|----------|---------|-------------|
| `enabled` | `false` | Master switch |
| `shard-db-count` | `1` | DB shards (1–10) |
| `shard-table-count` | `1` | Tables per DB (1–50) |
| `enable-table-secondary-index` | `false` | Use `{table}-{shardKey}-{tableIndex}` |
| `db-type` | `mysql` | `mysql` / `postgres` |
| `username` / `password` | — | Shared credentials |
| `entity-packages` | — | **Required** when enabled |
| `schema-path` | — | DDL template prefix |
| `template-table-names` | — | Template names |
| `table-registry-enabled` | `false` | Persist registry via JDBC |
| `table-registry-table` | `angus_shard_table` | Registry table name |
| `mysql.urls` / `postgresql.urls` | — | JDBC URLs (≥ `shard-db-count`) |
| `aspect-pointcut` | — | Unused / deprecated |

Hikari: `angus.sharding.hikari.*`.

---

## 5. Integration Guide

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.sharding-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

1. Annotate entities with `@ShardedTable`.
2. Bind shard repos to `shardingEntityManagerFactory` / `shardingTransactionManager`.
3. Annotate query methods with `@Sharding(shardKey = "tenantId")`.
4. Provide DDL templates under `schema-path`.
5. For embedded mode: register `ShardedRepositoryAspect` bean yourself.
6. Optional: override `ShardingStrategy` with `HashShardingStrategy`.

Use `@Param` or `-parameters` so key resolution does not silently fall back to `0`.

---

## 6. API Reference

Annotations: `@Sharding`, `@ShardedTable`, `@ShardedRepository`.

SPI: `ShardingStrategy`, `ShardKeyResolver`, `ShardTableManager`, `ShardTableRegistry`.

Context: `ShardContext` / `ShardInfo`; `isSharded()` when key ≠ `"dataSource"`.

Naming: `exec_sample-100` or `exec_sample-100-3`.

---

## 7. Routing & Naming

**Full mode:** `shard{dbIndex}DataSource` + `{table}-{shardKey}[-{tableIndex}]`.  
**Embedded mode:** tableIndex stored in `ShardInfo.shardKey` slot; rewrite `{table}-{tableIndex}`;
DS miss falls back to primary.

SQL matcher covers SELECT/INSERT/UPDATE/DELETE only.

---

## 8. Migration from metricsds

`@ShardingTable` → `@ShardedTable`; `xcan.datasource.metrics.*` → `angus.sharding.*`;
EMF beans → `shardingEntityManagerFactory` / `shardingTransactionManager`.

---

## 9. Best Practices

1. Shard repos on sharding EMF; registry repos on primary EMF.
2. Always `CREATE TABLE IF NOT EXISTS`.
3. Prefer explicit `@Param` for shard keys.
4. Do not mix shard keys in one `saveAll`.
5. Register `ShardedRepositoryAspect` for embedded mode.
6. Never change shard counts / hash algorithm after production data exists.

---

## 10. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Nothing loads | `enabled=false` | Set `true` |
| EMF fails | Missing `entity-packages` | Configure packages |
| Hits primary DS | Wrong EMF binding | Fix `@EnableJpaRepositories` |
| Always shard0 | Param name unresolved | `-parameters` / `@Param` |
| Embedded no-op | Aspect not a bean | Register `ShardedRepositoryAspect` |

---

## Related classes

| Class | Module |
|-------|--------|
| `cloud.xcan.angus.sharding.ShardingAutoConfiguration` | `sharding-starter` |
| `cloud.xcan.angus.sharding.ShardingAspect` | `sharding-starter` |
| `cloud.xcan.angus.sharding.annotation.Sharding` | `sharding-core` |
| `cloud.xcan.angus.sharding.context.ShardContext` | `sharding-core` |
