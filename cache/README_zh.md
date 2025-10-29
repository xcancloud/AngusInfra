# AngusInfra — 缓存模块

## 概述

`cache` 模块实现了一个混合两级缓存（内存 + 持久化），用于提供：

- 对热数据的快速内存访问
- 持久化后端（JPA）用于数据持久化与多实例共享
- 统一的管理 API 用于监控与管理
- 通过事务代理（TransactionalDistributedCache）保证与持久化交互的安全性

## 模块结构

- `core` — 核心缓存接口与实现（例如 `HybridCacheManager`、`MemoryCache`、`CachePersistence`）。
- `starter` — Spring Boot starter，提供自动配置、Spring Data JPA 适配器和管理 REST 控制器。

## 关键接口 / 类

- `cloud.xcan.angus.cache.IDistributedCache` — 应用使用的公共缓存 API。
- `cloud.xcan.angus.cache.HybridCacheManager` — 将内存缓存与持久化结合的核心实现。
- `cloud.xcan.angus.cache.MemoryCache` — 内存缓存实现。
- `cloud.xcan.angus.cache.CachePersistence` — 持久化抽象接口。
- `cloud.xcan.angus.cache.entry.CacheEntry` / `CacheEntryRepository` — 持久化实体与仓库。
- `cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository` — Spring Data 仓库（starter 模块）。
- `cloud.xcan.angus.cache.autoconfigure.HybridCacheAutoConfiguration` — 自动配置类，根据仓库是否存在装配持久化与管理控制器。
- `cloud.xcan.angus.cache.management.CacheManagementController` — 管理 REST 控制器。

## 快速开始

### 1. 构建模块

在仓库根目录运行：

```bash
mvn -pl cache -am clean install
```

### 2. 在 Spring Boot 应用中引入 starter

示例 Maven 依赖：

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.cache-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

### 3. 持久化（可选）

如果类路径中存在 `SpringDataCacheEntryRepository`（即启用 Spring Data JPA 并扫描实体），starter 会自动配置 `CachePersistence` 适配器。否则缓存仅以内存行为为主。

若启用 JPA，请在 `application.yml` 中配置数据源。

## 管理 API

管理控制器映射在 `/api/v1/cache`，提供以下接口：

- `GET /api/v1/cache/stats` — 获取缓存统计信息（条目数、命中、未命中、内存大小等）
- `GET /api/v1/cache/{key}` — 获取缓存值（找不到时会以业务错误的形式在包装器中返回）
- `PUT /api/v1/cache/{key}` — 设置缓存值（JSON body 包含 `value` 和可选的 `ttlSeconds`）
- `DELETE /api/v1/cache/{key}` — 删除缓存键
- `GET /api/v1/cache/{key}/exists` — 检查键是否存在
- `GET /api/v1/cache/{key}/ttl` — 获取键的 TTL（秒），-1 = 永不过期，-2 = 未找到
- `POST /api/v1/cache/{key}/expire` — 为存在的键设置 TTL（JSON body：`ttlSeconds`）
- `POST /api/v1/cache/clear` — 清空所有缓存（内存 + 持久化）
- `POST /api/v1/cache/cleanup` — 从持久化存储清理过期项并返回删除数量

示例：通过 curl 设置缓存值

```bash
curl -X PUT -H "Content-Type: application/json" -d '{"value":"hello","ttlSeconds":60}' http://localhost:8080/api/v1/cache/my-key
```

## 构建与部署

- 在 Spring Boot 服务中包含 starter 来启用管理端点与可选的 JPA 持久化支持。
- 使用持久化存储时，确保 `entry` JPA 实体被扫描且仓库 bean 可用。

## 测试

运行模块的单元测试：

```bash
mvn -pl cache/core test
mvn -pl cache/starter test
```

## 贡献

欢迎提交 PR 与 issue。建议：

- 为变更添加单元测试
- 在可能的情况下保持 API 向后兼容
