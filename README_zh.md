# AngusInfra

[English](README.md) | [中文](README_zh.md)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-green)](https://spring.io/projects/spring-cloud)
[![Eureka Client](https://img.shields.io/badge/Eureka%20Client-4.2.0-lightgrey)](https://spring.io/projects/spring-cloud-netflix)
[![Open API](https://img.shields.io/badge/Open%20API-3.0.1-blue)](https://swagger.io/specification/)

**AngusInfra** 是一个基于SpringBoot的快速开发基础框架。该框架旨在简化和加速多租户应用的开发过程，使开发者能够更高效地构建可扩展、安全且易于维护的服务端应用。

## 主要特点

- 🌟 **OpenAPI支持**：支持并严格遵循 OpenAPI 规范，确保 API 开发和管理有广泛的社区支持。

- 🌟 **多租户支持**: AngusInfra 设计为共享数据源多租户架构，在无需担心数据隔离和安全性问题同时，帮助简化管理和降低硬件和运维成本。

- 🌟 **基于SpringBoot**: 采用 SpringBoot 框架进行业务化扩展，保证强大的后端功能和成熟的生态系统支持、提升快速的开发体验。

- 🌟 **快速开发**: AngusInfra 提供了一系列预构建的组件和模块，帮助开发者快速搭建应用基础，显著缩短开发周期。

    - 🚀 **通用业务处理**：提供高频业务场景的标准化处理模板和模版方法封装，有效降低 50% 以上的重复代码量。

    - 🚀 **丰富的工具类**：内置丰富的工具方法（如数据转换、校验工具链）和对主流中间件（Cache/Database/Remote）的二次封装，通过开箱即用的
      API 接口提升 50% 开发效率。

- 🌟 **易于扩展**: 框架的模块化设计允许开发者根据项目需求灵活扩展功能，支持自定义业务逻辑和用户界面。

- 🌟 **安全性考虑**: AngusInfra 在设计中注重安全性，提供多种身份验证和授权机制，确保用户数据和应用安全。

## 主要模块

### **核心模块**

| 模块         | 说明                                                             |
|------------|----------------------------------------------------------------|
| **`spec`** | 公共规范库，定义全局通用模型（DTO/枚举/错误码）、接口契约及跨模块通信协议 |
| **`core`** | 核心基础库，提供基础工具类、异常处理、多租户支持、业务模板及 Spring 扩展   |
| **`jdbc`** | Spring Data JDBC 集成库，支持多租户、批量操作及转换器工具              |

### **数据层模块**

| 模块               | 说明                                                                               |
|------------------|-----------------------------------------------------------------------------------|
| **`datasource`** | 数据源扩展库，基于 Spring JPA 提供多租户动态数据源配置、基本的分库分表策略及读写分离能力            |
| **`cache`**      | 二级缓存库，结合内存（Caffeine）与数据库（JPA）持久化，提供管理 REST API 及 Spring Boot 自动配置 |
| **`l2cache`**    | 二级缓存库，集成 Redis + Caffeine 实现高性能缓存，支持分布式一致性及缓存防止穿透策略           |
| **`lettucex`**   | Redis 增强库，统一 Lettuce 接入配置，提供业务扩展工具方法                                  |

### **分布式基础设施**

| 模块          | 说明                                                                                                 |
|-------------|-----------------------------------------------------------------------------------------------------|
| **`idgen`** | 分布式 ID 生成器，支持 Snowflake 变体 UidGenerator、自定义业务 ID（BidGenerator）及缓存策略              |
| **`job`**   | 基于数据库驱动的分布式任务调度框架，支持 SIMPLE、SHARDING、MAP_REDUCE 执行模型、分布式锁及管理 REST API |
| **`queue`** | 基于数据库的消息队列，实现租约式 SQS 风格语义，支持分区、死信队列、生命周期管理及 REST API               |

### **插件框架**

| 模块           | 说明                                                                              |
|--------------|----------------------------------------------------------------------------------|
| **`plugin`** | 可扩展插件框架，支持动态类加载、生命周期管理及热插拔（包含 api、core、starter 子模块） |

### **安全与认证**

| 模块                                    | 说明                                                        |
|---------------------------------------|-----------------------------------------------------------|
| **`security/auth-resource-model`**    | 授权持久化资源模型及认证相关 DTO 定义                         |
| **`security/auth-server-starter`**    | OAuth2 认证服务器，提供令牌颁发、密钥管理及授权端点自动配置       |
| **`security/auth-resource-starter`**  | OAuth2 资源服务器，支持密码模式、客户端凭证模式的资源访问鉴权     |
| **`security/auth-openapi2p-starter`** | 私有化 API 认证组件（OAuth2 客户端凭证模式），为 SaaS 私有化部署提供标准化鉴权方案 |
| **`security/auth-innerapi-starter`**  | 内部服务间认证组件（OAuth2 客户端凭证模式），保障微服务间通信安全   |

### **SpringBoot 快速集成**

| 模块                                      | 说明                                                                    |
|-----------------------------------------|-----------------------------------------------------------------------|
| **`integration/web-starter`**           | RESTful API 快速开发套件，自动配置统一响应格式、全局异常处理及跨域策略    |
| **`integration/oas3-starter`**          | OpenAPI 3.x 规范支持，自动生成接口文档并集成 Swagger UI                  |
| **`integration/jpa-starter`**           | Spring Data JPA 集成，包含通用 Repository、Specification 构建器及审计支持 |
| **`integration/feign-starter`**         | Spring Cloud OpenFeign 声明式 HTTP 客户端集成，含编解码及错误处理        |
| **`integration/observability-starter`** | 日志、指标及数据导出工具，服务于应用可观测性                               |

### **开发工具链**

| 模块              | 说明                                                                              |
|-----------------|---------------------------------------------------------------------------------|
| **`validator`** | 参数校验增强库，提供注解式校验规则与自定义校验器模板                                  |
| **`remote`**    | 基于 OpenFeign 的远程调用库扩展，包括传输对象DTO/VO/TO定义，统一返回结果数据格式，多语言支持等 |
| **`bom`**       | 依赖版本物料清单（Bill of Materials），统一管理组件版本与依赖冲突                   |

### **架构治理**

| 模块           | 说明                                        |
|--------------|-------------------------------------------|
| **`parent`** | Maven 父 POM，定义全局构建配置、插件管理及 Profile 策略 |
| **`docs`**   | 项目文档库，包含架构设计、模块说明及快速接入指南        |  

## 数据库脚本规范

为保证模块边界清晰、避免跨模块耦合，数据库定义脚本统一维护在各模块自身的 `core` 子模块
`src/main/resources/schema` 目录下。

当前已落地的模块如下：

| 模块 | MySQL 脚本 | PostgreSQL 脚本 |
|------|------------|-----------------|
| `cache` | `cache/core/src/main/resources/schema/mysql/cache-schema.sql` | `cache/core/src/main/resources/schema/postgres/cache-schema.sql` |
| `idgen` | `idgen/core/src/main/resources/schema/mysql/idgen-schema.sql` | `idgen/core/src/main/resources/schema/postgres/idgen-schema.sql` |
| `job` | `job/core/src/main/resources/schema/mysql/job-schema.sql` | `job/core/src/main/resources/schema/postgres/job-schema.sql` |
| `queue` | `queue/core/src/main/resources/schema/mysql/queue-schema.sql` | `queue/core/src/main/resources/schema/postgres/queue-schema.sql` |

推荐初始化配置示例：

```yaml
# MySQL
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/mysql/<module>-schema.sql

# PostgreSQL
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema/postgres/<module>-schema.sql
```

## 适用场景

💡 AngusInfra 特别适合于需要多租户支持的 SaaS 应用、企业内部系统和其他需要快速开发和部署的 Web 应用。

## 开源协议

📜 本项目采用 [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html) 开源协议。


