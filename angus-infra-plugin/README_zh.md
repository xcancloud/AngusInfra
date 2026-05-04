# AngusInfra — Plugin 模块

[English](README.md) | [中文](README_zh.md)

## 目录

1. 模块概述
2. 架构设计
3. 核心组件
4. 存储与元数据模型
5. 配置参考
6. 三方接入说明
7. 管理 API
8. 注意事项

---

## 1. 模块概述

`plugin` 模块为 AngusInfra 提供一套轻量级 Java 插件框架，用于在宿主应用运行时加载外部插件 JAR，并为插件提供：

- 插件描述文件解析（`plugin.json`）
- 隔离类加载（`PluginClassLoader`）
- 生命周期管理（`initialize / start / stop / destroy`）
- 插件运行上下文（配置、数据目录、日志、服务注册）
- 动态 REST 控制器注册与卸载
- 插件包持久化存储（磁盘 / JPA）
- 管理接口（安装、删除、查询、统计）

模块分为 5 个子模块：

| 子模块        | 作用                              |
|------------|---------------------------------|
| `api`      | 定义插件 SPI、模型、事件与异常               |
| `core`     | 插件运行时核心实现，包括默认管理器、类加载器、上下文与存储接口 |
| `starter`  | Spring Boot 自动装配、JPA 存储支持、管理控制器 |
| `examples` | 示例插件工程，如 GitHub / Jenkins 插件    |
| `bom`      | 依赖版本统一管理                        |

从源码实现看，这个模块的目标不是做复杂插件市场，而是为宿主系统提供“可装载、可隔离、可动态暴露接口”的扩展点基础设施。

---

## 2. 架构设计

### 2.1 模块分层

```text
宿主 Spring Boot 应用
  ├─ 引入 xcan-angusinfra.plugin-starter
  ├─ 配置 angus.plugin.*
  ├─ 提供 Spring MVC 环境（可选，用于动态 REST）
  └─ 通过管理 API 安装 / 删除 / 查询插件

starter
  ├─ PluginAutoConfiguration
  ├─ StarterPluginProperties
  ├─ PluginManagementController
  ├─ JpaPluginStore（可选）
  └─ PluginManagementService

core
  ├─ DefaultPluginManager
  ├─ PluginClassLoader
  ├─ DefaultPluginContext
  ├─ DynamicRestEndpointManager
  ├─ PluginStore / DiskPluginStore
  └─ PluginWrapper

api
  ├─ Plugin / RestfulPlugin / PluginManager / PluginContext
  ├─ PluginController
  ├─ PluginDescriptor / PluginInfo / PluginState
  └─ PluginLoadedEvent / PluginStartedEvent / PluginStoppedEvent / PluginUnloadedEvent / PluginFailedEvent
```

### 2.2 插件加载流程

```text
安装插件（上传 JAR 或写入插件目录）
    ↓
PluginStore 持久化插件包（DISK 或 JPA）
    ↓
DefaultPluginManager.loadPlugin(path)
    ↓
从 JAR 读取 plugin.json
    ↓
校验 descriptor（id、pluginClass、依赖、最小版本、权限）
    ↓
创建 PluginClassLoader
    ↓
反射实例化插件主类（必须实现 Plugin）
    ↓
创建 DefaultPluginContext
    ↓
调用 plugin.initialize(context)
    ↓
若实现 RestfulPlugin，则动态注册控制器 RequestMapping
    ↓
保存 PluginWrapper 与 PluginInfo 信息
    ↓
发布 PluginLoadedEvent
```

### 2.3 生命周期说明

插件接口 `Plugin` 定义了完整生命周期：

- `initialize(PluginContext context)`：初始化插件上下文与一次性资源
- `start()`：启动插件运行时逻辑
- `stop()`：停止插件运行时逻辑
- `destroy()`：释放资源，准备卸载

需要注意当前实现中的一个边界：

- `DefaultPluginManager.loadPlugin()` 会执行 `initialize()`，并在插件实现了 `RestfulPlugin` 时注册动态
  REST 端点。
- 但 `loadPlugin()` 本身不会自动调用 `start()`。
- `startPlugin()` / `stopPlugin()` / `reloadPlugin()` 已在 `PluginManager` 和 `DefaultPluginManager`
  中实现，但 starter 暴露的默认管理 REST API 当前没有对应的启停接口。

因此，当前 starter
场景更适合“加载后即可提供接口”的插件，或由宿主代码直接调用 `PluginManager#startPlugin()` 管理更细粒度生命周期。

### 2.4 动态 REST 暴露模型

如果插件实现 `RestfulPlugin`：

- 插件通过 `getControllerClasses()` 返回控制器类列表。
- `DynamicRestEndpointManager` 会反射创建控制器实例。
- 若控制器继承 `PluginController`，框架会注入 `PluginContext`。
-

再扫描 `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping` / `@RequestMapping`。

- 最终以 `plugin.getApiPrefix()`
  作为统一前缀，把插件控制器挂到宿主应用的 `RequestMappingHandlerMapping` 上。

默认前缀规则：

```text
/api/plugins/{pluginId}
```

例如示例 GitHub 插件控制器方法路径是 `/repos/readme`，那么最终暴露路径是：

```text
/api/plugins/angus-plugin-github/repos/readme
```

### 2.5 存储后端切换

插件包持久化由 `PluginStore` 抽象定义，当前有两种实现：

| 实现                | 说明                                        |
|-------------------|-------------------------------------------|
| `DiskPluginStore` | 插件 JAR 直接保存到本地目录                          |
| `JpaPluginStore`  | 插件二进制保存到数据库 `plugin_files` 表，加载时写入临时文件再装载 |

自动装配优先级：

- 当 `angus.plugin.storage-type=JPA` 且 Spring 容器中存在 `PluginRepository`
  时，使用 `JpaPluginStore`
- 否则回退到 `DiskPluginStore`

---

## 3. 核心组件

### 3.1 API 层接口

#### Plugin

插件主接口，所有插件必须实现。必须提供：

- 唯一标识 `getId()`
- 基本元数据 `getName()` / `getVersion()` / `getDescription()` / `getAuthor()`
- 生命周期方法 `initialize()` / `start()` / `stop()` / `destroy()`
- 当前状态 `getState()`

#### RestfulPlugin

用于声明“插件会暴露 REST 控制器”。在 `Plugin` 基础上增加：

- `getControllerClasses()`：返回控制器类列表
- `getApiPrefix()`：返回 API 前缀，默认 `/api/plugins/{pluginId}`
- `enableApiDoc()`：是否允许暴露插件文档，默认 `true`

#### PluginContext

插件运行时上下文，提供：

- 获取宿主 `ApplicationContext`
- 获取插件配置 `Map<String, Object>`
- 获取插件数据目录 `getDataDirectory()`
- 通过宿主日志记录插件日志
- 插件间服务注册与发现
- 获取宿主 Bean
- 获取环境变量

#### PluginController

插件控制器基类，主要提供：

- 自动持有 `PluginContext`
- 快捷日志输出 `log()`
- 快捷读取插件配置 `getConfig()`
- 快捷获取注册服务 `getService()`
- 懒加载宿主 `ApplicationContext`

### 3.2 DefaultPluginManager

这是插件系统的核心实现，负责：

- 初始化插件目录和数据目录
- 扫描并加载全部插件
- 安装插件到存储后端并即时装载
- 卸载插件并关闭类加载器
- 读取 `plugin.json` 并反射创建插件实例
- 维护内存中的 `PluginWrapper` 和 `PluginClassLoader`
- 发布插件生命周期事件

内部维护两个关键 `ConcurrentHashMap`：

- `plugins`：`pluginId -> PluginWrapper`
- `classLoaders`：`pluginId -> PluginClassLoader`

### 3.3 PluginClassLoader

`PluginClassLoader` 继承 `URLClassLoader`，每个插件使用独立类加载器，达到：

- 插件 JAR 与宿主隔离
- 不同插件间依赖隔离
- 卸载时可关闭类加载器释放资源

若 `plugin.json` 中声明了 `libraries`，管理器还会尝试扫描 `${pluginId}-lib` 目录，把附加 JAR 加入类加载路径。

### 3.4 DefaultPluginContext

默认上下文实现具备三类能力：

- 配置合并：默认配置 + `plugin.json` 配置 + 宿主按插件覆盖配置
- 目录隔离：数据目录为 `${dataDirectory}/{pluginId}`
- 服务注册：通过内存 `ConcurrentHashMap` 保存插件发布的服务

当前 `getEnvironment()` 读取的是系统环境变量 `System.getenv()`，不是 Spring `Environment`。

### 3.5 DynamicRestEndpointManager

负责插件控制器的动态注册与反注册：

- 注册时通过 `requestMappingHandlerMapping.registerMapping(...)` 动态挂载路由
- 卸载时反向执行 `unregisterMapping(...)`
- 如果控制器实现 `AutoCloseable`，卸载时会自动调用 `close()`
- 如果控制器继承 `PluginController`，卸载时会清空上下文引用

框架会把每个插件注册过的 endpoint 存在内存中，用于统计和卸载清理。

### 3.6 管理服务

`PluginManagementServiceImpl` 是对 `PluginManager` 的薄封装，提供：

- `initialize()`
- `reloadAll()`
- `install(pluginId, bytes)`
- `remove(pluginId, fromStore)`
- `listPlugins()`
- `getPlugin(pluginId)`
- `stats()`

`stats()` 当前统计口径：

- `totalPlugins`：当前内存中已知插件数
- `activePlugins`：状态为 `STARTED` 的插件数
- `restEndpoints`：所有插件动态注册 endpoint 总数

---

## 4. 存储与元数据模型

### 4.1 plugin.json 描述文件

每个插件 JAR 至少需要在根目录或 `META-INF/plugin.json` 中提供插件描述文件。

当前支持字段：

| 字段                    | 说明          |
|-----------------------|-------------|
| `id`                  | 插件唯一标识      |
| `name`                | 插件名称        |
| `version`             | 插件版本        |
| `description`         | 插件说明        |
| `author`              | 作者          |
| `pluginClass`         | 插件入口类，全限定类名 |
| `dependencies`        | 依赖插件 ID 列表  |
| `libraries`           | 外挂依赖库列表     |
| `configuration`       | 插件默认配置      |
| `minSystemVersion`    | 宿主最小系统版本    |
| `requiredPermissions` | 所需权限列表      |
| `homepage`            | 插件主页        |
| `license`             | 许可证         |
| `tags`                | 标签列表        |

最小示例：

```json
{
  "id": "angus-plugin-github",
  "pluginClass": "cloud.xcan.angus.plugin.examples.github.GitHubPlugin",
  "version": "1.0.0",
  "name": "Angus GitHub Example Plugin"
}
```

### 4.2 PluginInfo

`PluginInfo` 是宿主对外展示的运行时视图，包含：

- 基本元数据：`id/name/version/description/author`
- 生命周期：`state/loadedAt/startedAt`
- 插件类型与入口：`pluginClass/type/apiPrefix`
- REST 能力：`endpointCount`
- 文件信息：`filePath/fileSize`

### 4.3 PluginState

生命周期状态枚举：

- `UNKNOWN`
- `LOADING`
- `INITIALIZED`
- `STARTED`
- `STOPPED`
- `UNLOADING`
- `ERROR`

### 4.4 JPA 存储表

当使用 `JpaPluginStore` 时，插件包二进制写入 `plugin_files` 表：

| 字段           | 说明              |
|--------------|-----------------|
| `id`         | 插件 ID，主键        |
| `name`       | 插件名称            |
| `version`    | 插件版本            |
| `data`       | JAR 二进制内容（BLOB） |
| `uploadedAt` | 上传时间            |

---

## 5. 配置参考

配置前缀为 `angus.plugin`。

```yaml
angus:
  plugin:
    enabled: true
    storage-type: DISK
    auto-load: true
    max-upload-size: 52428800
    enable-management-api: true
    management-api-prefix: /api/plugins
    enable-security-check: true
    allowed-sources:
      - "*"
    scan-interval: 30000
    validate-on-startup: true
    default-configuration:
      system.version: 1.0.0
    plugin-configurations:
      angus-plugin-github:
        github.api.base-url: https://api.github.com
```

配置项说明：

| 配置项                     | 默认值             | 说明                                                                                      |
|-------------------------|-----------------|-----------------------------------------------------------------------------------------|
| `enabled`               | `true`          | 是否启用插件自动装配                                                                              |
| `storage-type`          | `DISK`          | 插件存储后端，支持 `DISK` / `JPA`                                                                |
| `directory`             | `./plugins`     | 磁盘存储时插件 JAR 目录                                                                          |
| `data-directory`        | `./plugin-data` | 插件运行数据目录根路径                                                                             |
| `auto-load`             | `true`          | 启动时是否自动扫描并加载插件                                                                          |
| `max-upload-size`       | `52428800`      | 管理接口允许上传的最大文件大小                                                                         |
| `enable-management-api` | `true`          | 管理 API 是否启用。当前代码中仅用于配置承载，没有单独条件开关控制 Controller 注册                                       |
| `management-api-prefix` | `/api/plugins`  | 管理 API 前缀配置。当前 `PluginManagementController` 路径写死为 `/api/v1/plugin-management`，该属性尚未真正生效 |
| `enable-security-check` | `true`          | 是否输出权限检查相关日志                                                                            |
| `allowed-sources`       | `*`             | 允许的插件来源列表。当前代码中未看到强制校验逻辑                                                                |
| `scan-interval`         | `30000`         | 预留扫描间隔配置，当前未见热加载扫描线程实现                                                                  |
| `validate-on-startup`   | `true`          | 启动或加载时是否执行插件描述校验                                                                        |
| `default-configuration` | `{}`            | 注入所有插件的默认配置                                                                             |
| `plugin-configurations` | `{}`            | 按插件 ID 覆盖配置                                                                             |

---

## 6. 三方接入说明

### 6.1 引入依赖

宿主应用一般直接引入 starter：

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.plugin-starter</artifactId>
  <version>3.0.0</version>
</dependency>
```

如果你只想在业务工程里开发插件 API，而不需要宿主运行时，可只引入 `plugin-api`。

### 6.2 场景一：作为宿主系统接入插件框架

最小要求：

- Spring Boot 应用
- 如果需要动态 REST 插件，宿主需具备 Spring MVC 环境
- 如果需要 JPA 存储，宿主需配置数据源并启用 JPA

基础配置示例：

```yaml
angus:
  plugin:
    enabled: true
    storage-type: DISK
    auto-load: true
```

当应用启动后：

- `PluginAutoConfiguration` 自动装配
- 创建 `PluginStore`
- 创建 `DefaultPluginManager`
- 创建 `PluginManagementService`

若你希望应用启动时立即扫描插件，需要在宿主应用中显式调用：

```java
import cloud.xcan.angus.plugin.management.PluginManagementService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class PluginBootstrap {

  private final PluginManagementService pluginManagementService;

  public PluginBootstrap(PluginManagementService pluginManagementService) {
    this.pluginManagementService = pluginManagementService;
  }

  @PostConstruct
  public void init() {
    pluginManagementService.initialize();
  }
}
```

> 当前 starter 中没有看到自动调用 `PluginManager.initialize()`
> 的生命周期钩子，因此若宿主不主动初始化，`auto-load` 配置本身不会触发加载。

### 6.3 场景二：开发一个最小插件

插件实现：

```java
import cloud.xcan.angus.plugin.api.Plugin;
import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginState;

public class HelloPlugin implements Plugin {

  private PluginState state = PluginState.UNKNOWN;

  @Override
  public String getId() {
    return "hello-plugin";
  }

  @Override
  public String getName() {
    return "Hello Plugin";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String getDescription() {
    return "A minimal Angus plugin";
  }

  @Override
  public String getAuthor() {
    return "Angus Team";
  }

  @Override
  public void initialize(PluginContext context) throws PluginException {
    context.log("INFO", "Hello plugin initialized");
    state = PluginState.INITIALIZED;
  }

  @Override
  public void start() throws PluginException {
    state = PluginState.STARTED;
  }

  @Override
  public void stop() throws PluginException {
    state = PluginState.STOPPED;
  }

  @Override
  public void destroy() throws PluginException {
    state = PluginState.UNLOADING;
  }

  @Override
  public PluginState getState() {
    return state;
  }
}
```

对应 `plugin.json`：

```json
{
  "id": "hello-plugin",
  "name": "Hello Plugin",
  "version": "1.0.0",
  "description": "A minimal Angus plugin",
  "author": "Angus Team",
  "pluginClass": "com.example.plugin.HelloPlugin"
}
```

打包后将 `plugin.json` 放在 JAR 根目录或 `META-INF/plugin.json`。

### 6.4 场景三：开发 REST 插件

插件主类实现 `RestfulPlugin`：

```java
import cloud.xcan.angus.plugin.api.RestfulPlugin;
import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.model.PluginState;
import java.util.List;

public class HelloRestPlugin implements RestfulPlugin {

  private PluginState state = PluginState.UNKNOWN;

  @Override
  public String getId() {
    return "hello-rest-plugin";
  }

  @Override
  public String getName() {
    return "Hello Rest Plugin";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String getDescription() {
    return "Expose REST endpoints dynamically";
  }

  @Override
  public String getAuthor() {
    return "Angus Team";
  }

  @Override
  public void initialize(PluginContext context) {
    state = PluginState.INITIALIZED;
  }

  @Override
  public void start() {
    state = PluginState.STARTED;
  }

  @Override
  public void stop() {
    state = PluginState.STOPPED;
  }

  @Override
  public void destroy() {
    state = PluginState.UNLOADING;
  }

  @Override
  public PluginState getState() {
    return state;
  }

  @Override
  public List<Class<?>> getControllerClasses() {
    return List.of(HelloController.class);
  }
}
```

控制器实现：

```java
import cloud.xcan.angus.plugin.api.PluginController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController extends PluginController {

  @GetMapping("/hello")
  public String hello() {
    log("INFO", "hello endpoint called");
    return "plugin=" + getPluginId();
  }
}
```

最终路由为：

```text
/api/plugins/hello-rest-plugin/hello
```

### 6.5 场景四：使用管理 API 安装插件

默认管理控制器路径：

```text
/api/v1/plugin-management
```

安装接口：

```bash
curl -X POST \
  -F "pluginId=hello-plugin" \
  -F "file=@./hello-plugin.jar" \
  http://localhost:8080/api/v1/plugin-management/install
```

安装流程：

- 校验上传文件大小是否超出 `max-upload-size`
- 写入 `PluginStore`
- 调用 `PluginManager.installPlugin()`
- 内部继续执行 `loadPlugin()`
- 返回 `PluginInfo`

### 6.6 场景五：切换 JPA 存储后端

当你不希望插件包落本地磁盘时，可以使用 JPA 存储：

```yaml
angus:
  plugin:
    enabled: true
    storage-type: JPA

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/angus
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
```

工作方式：

- 上传时 JAR 字节写入 `plugin_files.data`
- 加载时 `JpaPluginStore` 会把二进制写入临时文件
- `DefaultPluginManager` 再从临时文件中读取 `plugin.json` 并装载
- 卸载时若识别为 JPA spill file，会尝试删除临时 JAR

### 6.7 场景六：宿主代码直接管理插件

如果你需要启停/重载功能，可以直接注入 `PluginManager`：

```java
import cloud.xcan.angus.plugin.api.PluginManager;
import org.springframework.stereotype.Service;

@Service
public class PluginOpsService {

  private final PluginManager pluginManager;

  public PluginOpsService(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  public void reload(String pluginId) throws Exception {
    pluginManager.reloadPlugin(pluginId);
  }

  public void start(String pluginId) throws Exception {
    pluginManager.startPlugin(pluginId);
  }

  public void stop(String pluginId) throws Exception {
    pluginManager.stopPlugin(pluginId);
  }
}
```

这部分能力当前没有被默认管理 Controller 暴露出来，但框架内核已支持。

---

## 7. 管理 API

`PluginManagementController` 默认提供以下接口：

| 方法       | 路径                                     | 说明         |
|----------|----------------------------------------|------------|
| `POST`   | `/api/v1/plugin-management/install`    | 上传并安装插件    |
| `DELETE` | `/api/v1/plugin-management/{pluginId}` | 卸载并可选删除插件包 |
| `GET`    | `/api/v1/plugin-management/{pluginId}` | 查询插件详情     |
| `GET`    | `/api/v1/plugin-management/list`       | 查询全部插件     |
| `GET`    | `/api/v1/plugin-management/stats`      | 查询插件统计     |

返回结构使用 `ApiLocaleResult` 包装。

统计接口返回：

- `totalPlugins`
- `activePlugins`
- `restEndpoints`

---

## 8. 注意事项

1. `pluginId` 必须匹配正则 `[a-zA-Z0-9._\-]+`，否则安装和存储会失败。
2. 插件入口类必须实现 `Plugin`，否则 `DefaultPluginManager` 会拒绝加载。
3. `plugin.json` 至少需要提供 `id` 和 `pluginClass`，否则校验失败。
4. 依赖插件 `dependencies` 当前只做告警日志，不会阻止加载，也没有自动拓扑排序。
5. `minSystemVersion` 当前只做版本比较与告警，不会强制阻止加载。
6. `requiredPermissions` 当前主要用于日志输出，未看到真正的权限裁决逻辑。
7. `management-api-prefix`、`enable-management-api`、`allowed-sources`、`scan-interval`
   在当前代码中更多是配置预留位，尚未完全贯穿到控制器路径、接口开关、来源校验和热加载调度中。
8. `loadPlugin()` 会执行 `initialize()` 和动态 REST 注册，但不会自动 `start()`
   ；如果插件业务依赖 `start()`，需要宿主主动调用 `PluginManager.startPlugin()`。
9. 使用 `JpaPluginStore` 时，插件加载依赖临时文件中转，不适合把超大插件包频繁装载/卸载作为高频操作。
10. 动态注册的控制器需要保证路径不与宿主应用现有接口冲突，否则注册阶段可能抛出映射冲突异常。
