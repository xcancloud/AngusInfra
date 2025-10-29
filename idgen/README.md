ID Generator
==========================

> Note: AngusInfra ID Generator is a secondary development based on
> Baidu's [UidGenerator](https://github.com/baidu/uid-generator).

## Main Modifications

1. Refactored the package structure and optimized some code logic.
2. Changed the default persistence method from MyBatis to JPA.
3. Changed the default configuration method from Spring to Spring Boot.
4. Modified the table design to remove strong dependency on MySQL, enabling support for databases
   such as Postgres DB, Oracle, and SQL Server.
5. Introduced a human-readable business ID generator: BidGenerator.

## UidGenerator

UidGenerator is a Java implemented, [Snowflake](https://github.com/twitter/snowflake) based unique
ID generator. It
works as a component, and allows users to override workId bits and initialization strategy. As a
result, it is much more
suitable for virtualization environment, such as [docker](https://www.docker.com/). Besides these,
it overcomes
concurrency limitation of Snowflake algorithm by consuming future time; parallels UID produce and
consume by caching
UID with RingBuffer; eliminates CacheLine pseudo sharing, which comes from RingBuffer, via padding.
And finally, it
can offer over <font color=red>6 million</font> QPS per single instance.

### Usage

Two generators are
provided: [DefaultUidGenerator](cloud.xcan.angus.idgen.uid.impl.DefaultUidGenerator.java)
and [CachedUidGenerator](cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator.java). If you require
high performance for UID generation, use `CachedUidGenerator`.

#### 1. Add Dependency

```xml
<dependency>
   <groupId>cloud.xcan.angus</groupId>
   <artifactId>xcan-infra.idgen-starter</artifactId>
   <version>2.0.0</version>
</dependency>
```

#### 2. Enable Auto-Configuration

```yml
xcan:
  idgen:
    enabled: true
```

#### 3. Create the `instance` Table

Run the SQL script to import the `instance` table. The script is as follows:

```sql
-- idgen-schema.sql
DROP DATABASE IF EXISTS `xxxx`;
CREATE DATABASE `xxxx`;
USE `xxxx`;
DROP TABLE IF EXISTS `xxxxx`.`instance`;
CREATE TABLE `xxxxx`.`instance` (
  `pk` varchar(40) NOT NULL,
  `id` bigint(21) NOT NULL,
  `host` varchar(160) NOT NULL DEFAULT '',
  `port` varchar(40) NOT NULL DEFAULT '',
  `instance_type` varchar(40) NOT NULL DEFAULT '',
  `create_date` datetime NOT NULL,
  `last_modified_date` datetime NOT NULL,
  PRIMARY KEY (`pk`),
  UNIQUE KEY `uidx_host_port` (`host`,`port`) USING BTREE
) ENGINE=InnoDB;
```

#### 4. Datasource Extended Configuration

```yaml
# Specify the JPA entity path for the `instance` entity
xcan.datasource.extra.entityPackages[0]=cloud.xcan.angus.idgen.entity
# Specify the path to the `instance` script. If the table does not exist, the script will automatically run when the application starts.
xcan.datasource.mysql.schema[0]=mysql/idgen-schema.sql
```

#### 5. Using CachedUidGenerator

- Business Layer (application)

```java
@Resource
protected CachedUidGenerator uidGenerator;
```

> Note: `CommCmd` is already injected by default!

- Facade Layer (facade)

```java
CachedUidGenerator uidGenerator = (CachedUidGenerator) SpringContextHolder.getBean("uidGenerator");
```

## BidGenerator

BidGenerator is designed to meet the requirements of business coding, including readability, global
uniqueness, sequential increment (or even continuity), and high performance. It supports two
generation modes based on DB and Redis, significantly improving performance through business
segmentation and pre-segmentation.

### Usage

#### 1. Add Dependency

```xml
<dependency>
   <groupId>cloud.xcan.angus</groupId>
   <artifactId>xcan-infra.idgen-starter</artifactId>
   <version>2.0.0</version>
</dependency>
```

#### 2. Enable Auto-Configuration

```yml
xcan:
  idgen:
    enabled: true
  # Required for Redis mode
  redis:
    enabled: true
```

#### 3. Configure Data Source and Redis

Refer to: `sample_library` or `AngusTester` configuration.

#### 4. Configure Bid in the Database Table `id_config`

- 4.1. Parameter Description

    - `bizKey`: The business identifier corresponding to the generated business code (bid).
    - `format`: Code format: `PREFIX_DATE_SEQ` - fixed prefix + date + sequence, `PREFIX_SEQ` -
      prefix + sequence, `DATE_SEQ` - date + sequence, `SEQ` - sequence.
    - `prefix`: Code prefix, supports 1-4 characters.
    - `dateFormat`: Code date format: `YYYY` - year, `YYYYMM` - year and month, `YYYYMMDD` - year,
      month, and day.
    - `seqLength`: Sequence length, maximum 40 digits, recommended length 8-12 digits. The specific
      length should be determined based on business data. If the length is greater than 0 and the
      current ID value is shorter than the set length, it will be left-padded with zeros (
      e.g., `T2021090100000001`). If the length is less than or equal to 0, the ID will be
      variable-length and
      auto-incremented. `Note: Setting a length means the generated ID will have a fixed length. Setting a length less than or equal to 0 means the ID will be variable-length.`
    - `mode`: Generation mode: `REDIS` - Redis-based, `DB` - database-based.
    - `scope`: Uniqueness scope: `PLATFORM` - unique across the platform, `TENANT` - unique within a
      tenant.
    - `tenantId`: The tenant to which the code belongs. For tenant scope, only one template data
      should be configured (with tenant ID `-1`). The ID generator will automatically generate
      configurations based on the tenant.
    - `maxId`: The current maximum ID in database mode (based on pre-segmented cache).
    - `step`: Segment step size, maximum allowed is 1,000,000, recommended range is 1,000 - 10,000.
      A smaller step size increases pressure on DB and Redis, potentially causing bottlenecks in DB
      mode. A larger step size may lead to unused segments being lost after a restart, resulting in
      large gaps in IDs.

**Note:**

1. The table `id_config` will be automatically created if it does not exist!
2. The recommended sequence length is 8-12 digits, and the recommended step size is 1,000 - 10,000.

- 4.2 Configuration Example

```sql
-- SEQ format + Redis mode + unique across the platform
INSERT INTO `id_config`(`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`, `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `last_modified_date`) 
  VALUES ('620d069a-c074-4b0e-9576-da2ed48092b2', 'biKey1002', 'SEQ', '', '', 8, 'REDIS', 'PLATFORM', -1, 0, 10000, '2021-09-03 19:01:40', '2021-09-03 19:01:40');
```

#### 5. BidGenerator Injection Example

- Business Layer (application)

```java
@Resource
BidGenerator bidGenerator;
```

- Facade Layer (facade)

```java
BidGenerator bidGenerator = (BidGenerator)SpringContextHolder.getBean("bidGenerator");
```

#### 6. BidGenerator API Usage Example

```java
/**
 * Get a single ID by business key
 */
String getId(String bizKey);

/**
 * Get a single ID by business key and tenant
 */
String getId(String bizKey, Long tenantId);

/**
 * Get a batch of IDs by business key and batch number
 */
List<String> getIds(String bizKey, int batchNum);

/**
 * Get a batch of IDs by business key, batch number, and tenant ID
 */
List<String> getIds(String bizKey, int batchNum, Long tenantId);
```

## Performance Result

- [UidGenerator Performance](performance/UIDPerformance.md)
- [BidGenerator Performance](performance/BIDPerformance.md)
