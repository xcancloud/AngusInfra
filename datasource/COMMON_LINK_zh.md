公共数据表
====

[English](COMMON_LINK.md) | [中文](COMMON_LINK_zh.md)

依托数据库 Federated/DBlink 技术，聚合各业务数据库公共数据，对外提供统一数据源和 JPA Repository
方式访问接口，JPA Repository 方式访问相比
Restful API 使用更加简单，性能更高。

***所有公共数据表统一通过 Federated/DBlink 逻辑存储在 commonlink 库，各业务服务配置连接到 commmon
库来统一访问。***

以下是 AngusTester 公共数据表配置和使用示例。

## 对外供提公共数据表

### MySQL 创建公共数据表

公共数据表使用 MySQL [联邦存储引擎](http://wiki.xcan.work/pages/viewpage.action?pageId=14647418)
方式来实现。

#### 1. 创建并授权用户

创建名称为 `commonlink` 用户，用于统一访问 Federated/DBlink server，每个业务数据库服务器都需要创建授权该用户。

##### 1.1 创建库和用户

```sql
-- 创建库
CREATE DATABASE `xcan_commonlink` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

-- 创建用户
CREATE USER 'commonlink'@'%' IDENTIFIED BY 'LUYSMvzdVR0';
grant all privileges on xcan_commonlink.* to commonlink@'%' identified by 'LUYSMvzdVR0';
FLUSH PRIVILEGES;
```

##### 1.2 用户授权，默认授权只读权限

```sql
GRANT select ON `xcan_gm`.`tenant` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`user0` TO `commonlink`@`%`;
```

#### 2. 创建联邦表

##### 2.1 创建联邦 server

***以 Dev 环境 dev-mw.xcan.cloud 为例，创建脚本如下：***

```sql 
-- GM 库
CREATE
SERVER xcan_gm_link
FOREIGN DATA WRAPPER mysql
OPTIONS (USER 'commonlink', PASSWORD 'LUYSMvzdVR0', HOST 'dev-mw.xcan.cloud', PORT 3306, DATABASE 'xcan_gm');
```

注意：线上环境需要改变 HOST（如：bj-c1-prod-mw1.xcan.cloud）和 PASSWORD。

##### 2.2 创建联邦表

***注意：联邦表是在 xcan_commonlink 库下创建。**

```sql
-- GM 库
CREATE TABLE `tenant`
(
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `no` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '编号',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '租户名称',
  `type` varchar(30) COLLATE utf8mb4_bin NOT NULL DEFAULT '-1' COMMENT '租户类型：-1-未知；1-个人；2-企业；3-政府及事业单位',
  `source` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '租户来源：PLAT_REGISTER-平台注册；BACK_ADD-后台添加；',
  `real_name_status` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '租户实名认证状态：PENDING-待审核,PASSED-审核通过(已实名),FAILURE-审核失败;',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '租户状态：1-启用；2-注销中；3-已注销；4-禁用；',
  `apply_cancel_date` datetime DEFAULT '2001-01-01 00:00:00' COMMENT '申请注销时间',
  `address` varchar(160) COLLATE utf8mb4_bin DEFAULT '' COMMENT '通讯地址',
  `user_count` int(11) NOT NULL DEFAULT '0' COMMENT '用户数',
  `locked` int(11) NOT NULL DEFAULT '0' COMMENT '锁定状态：0-未锁定；1-已锁定',
  `last_lock_date` datetime DEFAULT NULL COMMENT '最后锁定时间',
  `lock_start_date` datetime DEFAULT NULL COMMENT '锁定开始时间',
  `lock_end_date` datetime DEFAULT NULL COMMENT '锁定结束时间',
  `remark` varchar(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '备注',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_last_modified_date` (`last_modified_date`) USING BTREE,
  KEY `idx_apply_cancel_date` (`apply_cancel_date`) USING BTREE,
  KEY `idx_real_name_status` (`real_name_status`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_source` (`source`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_locked` (`locked`) USING BTREE,
  KEY `idx_lock_start_date` (`lock_start_date`) USING BTREE,
  KEY `idx_lock_end_date` (`lock_end_date`) USING BTREE
  -- FULLTEXT KEY `fx_name_no` (`name`,`no`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE = FEDERATED CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '租户'  CONNECTION = 'xcan_gm_link/tenant';
```

### PostgresSQL 创建公共数据表

略。

## 发布公共数据表

### 1. 提供数据表访问 API

- 方式1：在 API 模块 cloud.xcan.angus.api.commonlink 包下定义对外提供的公共数据表对应 domain 和
  repository 对象。
- 方式2：将已有代码移动到 api 对应 commonlink 包下，当已有持久层对象需要被外部项目使用时。

### 2. 发布 api 包

使用 maven deploy 命令将 api 模块发布到公共仓库或私服即可。

## 公共数据表访问

### 1. 引入依赖

在 Boot 模块添加自动装配依赖 `xcan-infra.core-starter`，包含公共表数据源和对应 JPA 配置。

```xml

<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.core-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

在 Core 模块添加项目公共数据表依赖，以 AngusGM 服务公共数据表 xcan-angusgm.api 为例：

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusgm.api</artifactId>
  <version>1.0.0</version>
</dependency>
```

引入后便可使用domain对象和持久化对象Repository访问所有公共数据表。注意：domain对象 和 repository对象
都在包路径 cloud.xcan.angus.api.commonlink 下。

### 2. 开启自动装配

配置 application.yml：

```yml
xcan:
  datasource:
    enabled: true
    commonlink:
      enabled: true
```

配置 application-${profile.active}.yml:

```yml
## local 环境配置示例
xcan:
  trace:
    enabled: true
  datasource:
    extra:
      dbType: '@databaseType@'
    hikari:
      readOnly: false
      connectionTestQuery: SELECT 1 FROM DUAL
      poolName: postgresHikariCP
      idleTimeout: 600000
      maxLifetime: 1800000
      maximumPoolSize: 30
      minimumIdle: 10
    commonlink:
      mysql:
        driverClassName: com.mysql.cj.jdbc.Driver
        type: com.zaxxer.hikari.HikariDataSource
        url: jdbc:mysql://${COMMON_MYSQL_HOST:dev-mw.xcan.cloud}:${COMMON_MYSQL_PORT:3306}/${COMMON_MYSQL_DB:xcan_common}?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=${info.app.timezone}&rewriteBatchedStatements=true
        username: ${COMMON_MYSQL_USER:commonlink}
        password: ${COMMON_MYSQL_PASSWORD:LUYSMvzdVR0}
```

### 3. SpringBoot 项目下使用示例

```java
@Resource
private UserRepo userRepo;
```
