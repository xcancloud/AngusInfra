# AngusInfra :: Persistence :: JDBC Starter

## 📋 概览

`persistence-jdbc-starter` 是 Spring Data JDBC 集成模块，提供：
- JDBC 数据访问封装
- 多租户数据隔离
- 审计日志
- 事务管理

## 🎯 设计目标

- **职责单一**: 仅处理 JDBC 持久化
- **独立可选**: 不需要 JDBC 的应用可不引入此模块
- **Spring Data 集成**: 基于 Spring Data JDBC
- **向后兼容**: 保持原有包名和 API

## 📦 包含内容

从 `core` 模块迁移以下内容：
- `core/jdbc/` → `persistence-jdbc-starter/jdbc/`

## 🚀 快速开始

### 添加依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.jdbc</artifactId>
  <version>2.0.0</version>
</dependency>

<!-- 数据库驱动（根据需要选择） -->
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
</dependency>
```

### 配置数据源

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 📚 迁移指南

### 从 core 模块迁移

**旧方式**（包含所有功能）:
```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.core</artifactId>
</dependency>
```

**新方式**（按需引入）:
```xml
<!-- 基础功能 -->
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.core</artifactId>
</dependency>

<!-- 仅使用 JDBC -->
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.jdbc</artifactId>
</dependency>
```

## 🔗 相关模块

- `xcan-angusinfra.core-base` - 核心基础（必需）
- `xcan-angusinfra.jpa-starter` - JPA 持久化（可选）
- `xcan-angusinfra.datasource` - 数据源管理（可选）

## 📝 版本历史

- **2.0.0** (2026-03-21) - 从 core 模块拆分出来

---

**维护者**: AngusInfra Team  
**许可**: GPLv3
