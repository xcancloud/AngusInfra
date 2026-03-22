# AngusInfra :: Core :: Base

## 📋 概览

`core-base` 是 AngusInfra 框架的核心基础模块，提供了所有业务应用的基础功能：
- 业务模板（BizTemplate）
- 异常处理体系
- 枚举定义
- 工具类库
- Spring 集成

此模块不包含任何持久化（JPA/JDBC）、远程调用（Feign）、消息队列等重型依赖，保持轻量和高内聚。

## 🎯 设计目标

- **单一职责**: 仅包含核心基础功能
- **轻量依赖**: 依赖 < 10 个核心库
- **高可复用**: 所有 Angus 应用的必选依赖
- **向后兼容**: 保持原有包名和 API

## 📦 主要功能

### 1. BizTemplate（业务模板）
提供标准的业务逻辑执行模板，支持：
- 参数校验
- 权限检查
- 多租户控制
- 链路追踪（TraceId）
- 执行耗时记录
- 异常处理

### 2. 异常体系（exception/）
统一的异常处理机制

### 3. 枚举定义（enums/）
通用枚举类型

### 4. 工具类（utils/）
常用工具方法集合

### 5. Spring 集成（spring/）
Spring 框架集成和扩展

## 🚀 快速开始

### 添加依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.core</artifactId>
  <version>3.0.0</version>
</dependency>
```

### 使用 BizTemplate

```java
public class CreateOrderBiz extends BizTemplate<Order> {
  
  @Override
  protected void checkParams() {
    // 参数校验
  }
  
  @Override
  protected Order process() {
    // 业务逻辑
    return new Order();
  }
}

// 执行
Order order = new CreateOrderBiz().execute();
```

## 📚 从 core 模块迁移

如果你之前依赖 `xcan-angusinfra.core`，现在可以：

### 选项 1：继续使用 core（推荐，向后兼容）
```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.core</artifactId>
</dependency>
```

### 选项 2：仅使用基础功能
```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.core</artifactId>
</dependency>
```

## 📦 模块结构

```
core-base/
├── src/main/java/cloud/xcan/angus/core/
│   ├── app/           # 应用上下文
│   ├── biz/           # BizTemplate
│   ├── enums/         # 枚举定义
│   ├── event/         # 事件定义
│   ├── exception/     # 异常体系
│   ├── spring/        # Spring 集成
│   └── utils/         # 工具类
└── pom.xml
```

## 🔗 相关模块

- `xcan-angusinfra.core` - 完整的 core 模块（聚合所有子模块）
- `xcan-angusinfra.jpa-starter` - JPA 持久化
- `xcan-angusinfra.jdbc` - JDBC 持久化
- `xcan-angusinfra.feign-starter` - Feign 远程调用

## 📝 版本历史

- **2.0.0** (2026-03-21) - 从 core 模块拆分出来

---

**维护者**: AngusInfra Team  
**许可**: GPLv3
