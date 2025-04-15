Public Data Tables
=====

[English](COMMON_LINK.md) | [中文](COMMON_LINK_zh.md)

Aggregate public data from various business databases using MySQL Federated/DBlink technology,
providing a unified data source and JPA Repository access interface. Compared to RESTful APIs, JPA
Repository offers simpler usage and higher performance.

***All public data tables are logically stored in the `commonlink` database via Federated/DBlink.
Each business service connects to the `common` database for unified access.***

Below is a configuration and usage example of AngusTester public data tables.

## Public Data Tables Provided

### Creating Public Tables in MySQL

Public tables are implemented using
MySQL [Federated Storage Engine](http://wiki.xcan.work/pages/viewpage.action?pageId=14647418).

#### 1. Create and Authorize Users

Create a user named `commonlink` for unified access to Federated/DBlink servers. This user must be
created and authorized on each business database server.

##### 1.1 Create Database and User

```sql
-- Create database
CREATE
DATABASE `xcan_commonlink` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

-- Create user
CREATE
USER 'commonlink'@'%' IDENTIFIED BY 'LUYSMvzdVR0';
GRANT ALL PRIVILEGES ON xcan_commonlink.* TO
'commonlink'@'%' IDENTIFIED BY 'LUYSMvzdVR0';
FLUSH
PRIVILEGES;
```

##### 1.2 Grant Read-Only Permissions by Default

```sql
GRANT SELECT ON `xcan_gm`.`tenant` TO `commonlink`@`%`;
```

#### 2. Create Federated Tables

##### 2.1 Create Federated Server

***Example for Dev environment (dev-mw.xcan.cloud):***

```sql 
-- GM database
CREATE
SERVER xcan_gm_link
FOREIGN DATA WRAPPER mysql
OPTIONS (
  USER 'commonlink', 
  PASSWORD 'LUYSMvzdVR0', 
  HOST 'dev-mw.xcan.cloud', 
  PORT 3306, 
  DATABASE 'xcan_gm'
);
```

Note: For production environments, update `HOST` (e.g., `bj-c1-prod-mw1.xcan.cloud`) and `PASSWORD`.

##### 2.2 Create Federated Tables

***Note: Federated tables are created under `xcan_commonlink` database.***

```sql
-- GM database
CREATE TABLE `tenant`
(
    `id`                 BIGINT(20) NOT NULL COMMENT 'Primary Key ID',
    `no`                 VARCHAR(20) COLLATE utf8mb4_bin  NOT NULL COMMENT 'Code',
    `name`               VARCHAR(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'Tenant Name',
    `type`               VARCHAR(30) COLLATE utf8mb4_bin  NOT NULL DEFAULT '-1' COMMENT 'Tenant Type: -1-Unknown; 1-Individual; 2-Enterprise; 3-Government',
    `source`             VARCHAR(20) COLLATE utf8mb4_bin  NOT NULL COMMENT 'Tenant Source: PLAT_REGISTER, BACK_ADD',
    `real_name_status`   VARCHAR(20) COLLATE utf8mb4_bin  NOT NULL DEFAULT '0' COMMENT 'Real-name Status: PENDING, PASSED, FAILURE',
    `status`             VARCHAR(20) COLLATE utf8mb4_bin  NOT NULL DEFAULT '0' COMMENT 'Status: 1-Enabled; 2-Canceling; 3-Canceled; 4-Disabled',
    `apply_cancel_date`  DATETIME                                  DEFAULT '2001-01-01 00:00:00' COMMENT 'Cancellation Application Date',
    `address`            VARCHAR(160) COLLATE utf8mb4_bin          DEFAULT '' COMMENT 'Address',
    `user_count`         INT(11) NOT NULL DEFAULT '0' COMMENT 'User Count',
    `locked`        INT(11) NOT NULL DEFAULT '0' COMMENT 'DistributedLock Status: 0-Unlocked; 1-Locked',
    `last_lock_date`     DATETIME                                  DEFAULT NULL COMMENT 'Last DistributedLock Date',
    `lock_start_date`    DATETIME                                  DEFAULT NULL COMMENT 'DistributedLock Start Date',
    `lock_end_date`      DATETIME                                  DEFAULT NULL COMMENT 'DistributedLock End Date',
    `remark`             VARCHAR(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'Remark',
    `created_by`         BIGINT(20) NOT NULL DEFAULT '-1' COMMENT 'Creator',
    `created_date`       DATETIME                         NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT 'Creation Date',
    `last_modified_by`   BIGINT(20) NOT NULL DEFAULT '-1' COMMENT 'Last Modifier',
    `last_modified_date` DATETIME                         NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT 'Last Modified Date',
    PRIMARY KEY (`id`) USING BTREE,
    KEY                  `idx_name` (`name`),
    KEY                  `idx_created_by` (`created_by`),
    -- ... (other indexes)
) ENGINE=FEDERATED CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Tenant' 
CONNECTION='xcan_gm_link/tenant';

-- Additional tables follow similar patterns...
```

### PostgreSQL Public Table Creation

(Skip as per original content)

## Publishing Public Tables

### 1. Provide Data Table APIs

- **Method 1**: Define domain objects and repositories in the `cloud.xcan.angus.api.commonlink`
  package.
- **Method 2**: Move existing persistence-layer objects to the `api.commonlink` package if needed by
  external projects.

### 2. Publish API Package

Deploy the API module to a public/private Maven repository using `mvn deploy`.

## Accessing Public Tables

### 1. Add Dependencies

Include the auto-configuration dependency in your Boot module:

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.core-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

Add the public table dependency (e.g., `xcan-angusgm.api`):

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusgm.api</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 2. Enable Auto-Configuration

Configure `application.yml`:

```yml
xcan:
  datasource:
    enabled: true
    commonlink:
      enabled: true
```

Example environment configuration (`application-local.yml`):

```yml
xcan:
  datasource:
    commonlink:
      mysql:
        driverClassName: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://${COMMON_MYSQL_HOST:dev-mw.xcan.cloud}/xcan_commonlink
        username: ${COMMON_MYSQL_USER:commonlink}
        password: ${COMMON_MYSQL_PASSWORD:LUYSMvzdVR0}
```

### 3. Usage in Spring Boot

Inject repositories directly:

```java
@Resource
private UserRepo userRepo;
```
