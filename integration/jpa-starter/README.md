# AngusInfra :: Persistence :: JPA Starter

## 📋 概览

`persistence-jpa-starter` 是 Spring Data JPA 集成模块，提供：

- JPA/Hibernate 数据访问
- 多租户数据隔离（过滤器）
- 审计日志（AuditingEntityListener）
- 乐观锁/悲观锁
- 事务管理

## 📦 包含内容

从 `core` 模块迁移以下内容：

- `core/jpa/` → `persistence-jpa-starter/jpa/`

## 🚀 快速开始

### 添加依赖

```xml

<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.jpa-stater</artifactId>
  <version>3.0.0</version>
</dependency>

  <!-- 数据库驱动 -->
<dependency>
<groupId>com.mysql</groupId>
<artifactId>mysql-connector-j</artifactId>
</dependency>
```

### 配置 JPA

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
```

## 📚 功能特性

### 多租户数据隔离

自动在查询中添加租户 ID 过滤：

```java

@Entity
@Table(name = "orders")
public class Order {

  @Id
  private Long id;

  private Long tenantId;  // 自动过滤

  // ...
}
```

### 审计日志

自动记录创建/更新时间和操作人：

```java

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Order {

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @CreatedBy
  private String createdBy;

  @LastModifiedBy
  private String updatedBy;
}
```

## 🔗 相关模块

- `xcan-angusinfra.core-base` - 核心基础（必需）
- `xcan-angusinfra.jdbc` - JDBC 持久化（可选）
- `xcan-angusinfra.datasource` - 数据源管理（可选）

---

**维护者**: AngusInfra Team  
**许可**: GPLv3
