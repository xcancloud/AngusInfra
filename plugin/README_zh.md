# AngusInfra — Plugin 模块

[English](README.md) | [中文](README_zh.md)

## 概述

`plugin` 模块为 AngusInfra 提供可扩展的插件框架，主要支持：

- 插件包的存储与管理（磁盘 / JPA 存储后端）
- 插件生命周期管理（安装、卸载、加载、卸载）
- 隔离类加载（`PluginClassLoader`）与插件运行上下文
- 动态注册 REST 端点（插件可在运行时暴露 API）
- 管理 API（上传、删除、列出、查询插件与统计信息）

## 模块结构

- `api` — 插件对外的接口与模型（例如 `Plugin`、`PluginContext`、`PluginManager`、`PluginInfo`）。
- `core` — 插件运行时核心实现（默认管理器、插件类加载器、存储接口与实现等）。
- `starter` — Spring Boot starter，包含自动配置、可选的 JPA 存储与管理控制器。
- `examples` — 示例插件工程（例如 `angus-plugin.github`、`angus-plugin.jenkins`）。
- `bom` — 依赖版本管理模块。

## 关键类 / 接口（示例）

- `cloud.xcan.angus.plugin.api.Plugin`
- `cloud.xcan.angus.plugin.api.PluginManager`
- `cloud.xcan.angus.plugin.api.PluginContext`
- `cloud.xcan.angus.plugin.model.PluginInfo` / `PluginDescriptor` / `PluginState`
- `cloud.xcan.angus.plugin.core.DefaultPluginManager`
- `cloud.xcan.angus.plugin.core.PluginClassLoader`
- `cloud.xcan.angus.plugin.store.PluginStore`（实现示例：`DiskPluginStore`、`JpaPluginStore`）
- `cloud.xcan.angus.plugin.core.DynamicRestEndpointManager`
- `cloud.xcan.angus.plugin.autoconfigure.PluginProperties`

## 快速开始

### 1. 构建模块

在项目根目录或 `AngusInfra` 子模块目录运行：

```bash
mvn -pl plugin -am clean install
```

### 2. 在 Spring Boot 应用中引入 starter

示例 Maven 依赖：

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.plugin-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

### 3. 配置（`application.yml` 示例）

Starter 会将配置绑定到 `StarterPluginProperties`（继承自 `PluginProperties`），配置前缀为 `angus.plugin`。

```yaml
angus:
  plugin:
    storage-type: DISK          # DISK 或 JPA
    directory: ./plugins       # 当使用磁盘存储时，插件 JAR 存放路径
    data-directory: ./plugin-data
    auto-load: true
    max-upload-size: 52428800  # 最大上传大小（字节）
    enable-management-api: true
    management-api-prefix: /api/plugins
    enable-security-check: true
    allowed-sources: ['*']
    scan-interval: 30000
    validate-on-startup: true

spring:
  datasource:
    url: jdbc:h2:mem:angusdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update
```

> 注：属性名采用 Spring Boot 的 kebab-case（如 `storage-type`、`data-directory` 等），对应 `PluginProperties` 中的字段。

## 管理 API

`starter` 模块包含管理控制器 `PluginManagementController`，其默认映射为：

`/api/v1/plugin-management`

主要 REST 接口：

- `POST /api/v1/plugin-management/install` — 上传并安装插件（multipart/form-data，参数：`pluginId`、`file`）
- `DELETE /api/v1/plugin-management/{pluginId}` — 卸载插件；可选查询参数 `removeFromStore`（默认 `true`）
- `GET /api/v1/plugin-management/{pluginId}` — 查询插件详情
- `GET /api/v1/plugin-management/list` — 列出所有插件
- `GET /api/v1/plugin-management/stats` — 获取插件系统统计信息

示例：使用 curl 上传插件（multipart）：

```bash
curl -v \
  -F "pluginId=my-plugin" \
  -F "file=@./my-plugin.jar" \
  http://localhost:8080/api/v1/plugin-management/install
```

## 插件构建与部署

1. 参考 `examples` 子模块中的示例插件（例如 `angus-plugin.github`、`angus-plugin.jenkins`）。
2. 每个示例插件包括 `plugin.json`（用于描述插件 id、版本、入口类等），将插件构建为 JAR。
3. 将 JAR 放入配置的插件目录（`angus.plugin.directory`，如 `./plugins`），或通过管理 API 上传并安装。
4. 插件安装后由 `PluginManager` 管理其生命周期；若 `auto-load=true`，服务启动时会自动加载目录下的插件。

## 测试

运行核心模块的单元测试：

```bash
mvn -pl plugin/core test
```

## 贡献

欢迎提交 issue 与 PR。建议遵循：

- 保持单元测试覆盖关键逻辑
- 遵循现有代码风格与 Lombok 使用约定
