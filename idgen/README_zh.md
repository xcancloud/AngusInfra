ID Generator
==========================

> 说明：AngusInfra UidGenerator 是基于百度 [UidGenerator](https://github.com/baidu/uid-generator)
> 二次开发来实现。

## 修改说明

1. 重构包结构、优化了部分代码逻辑；
2. 修改默认持久化方式 MyBatis 为 JPA；
3. 修改默认配置方式 Spring 为 SpringBoot;
4. 修改了表设计，去除了对 MySQL 强依赖，可支持 Postgres DB、Oracle、SQL Server等数据库。
5. 新增具有可读性的业务 ID 生成器-BidGenerator。

## UidGenerator

UidGenerator是Java实现的, 基于[Snowflake](https://github.com/twitter/snowflake)
算法的唯一ID生成器。UidGenerator以组件形式工作在应用项目中,
支持自定义workerId位数和初始化策略, 从而适用于[docker](https://www.docker.com/)等虚拟化环境下实例自动重启、漂移等场景。
在实现上, UidGenerator通过借用未来时间来解决sequence天然存在的并发限制; 采用RingBuffer来缓存已生成的UID,
并行化UID的生产和消费,
同时对CacheLine补齐，避免了由RingBuffer带来的硬件级「伪共享」问题. 最终单机QPS可达<font color=red>
600万</font>。

### 使用

提供了两种生成器: [DefaultUidGenerator](cloud.xcan.angus.idgen.uid.impl.DefaultUidGenerator.java)、[CachedUidGenerator](cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator.java)
。如对UID生成性能有要求, 请使用CachedUidGenerator。

#### 1. 引入依赖

```xml
<dependency>
   <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.idgen-starter</artifactId>
   <version>2.0.0</version>
</dependency>
```

#### 2. 开启自动装配

```yml
xcan:
  idgen:
    enabled: true
```

#### 3. 创建表 instance

运行sql脚本以导入表 instance, 脚本如下:

```sql
-- idgen-schema.sql
DROP TABLE IF EXISTS `instance`
CREATE TABLE `instance` (
  `pk` varchar(40)  NOT NULL,
  `id` bigint(21) NOT NULL,
  `host` varchar(160)  NOT NULL DEFAULT '',
  `port` varchar(40)  NOT NULL DEFAULT '',
  `instance_type` varchar(40)  NOT NULL DEFAULT '',
  `create_date` datetime NOT NULL ,
  `last_modified_date` datetime NOT NULL ,
  PRIMARY KEY (`pk`),
  UNIQUE KEY `uidx_host_port` (`host`,`port`)  USING BTREE,
) ENGINE=InnoDB;
```

#### 4. Datasource 扩展配置

```yaml
# 指定 instance 实体 JPA entity 路径
xcan.datasource.extra.entityPackages[0]=cloud.xcan.angus.idgen.entity
# 指定 instance 脚本路径，配置后应用启动时如果不存在会自动运行脚本
xcan.datasource.mysql.schema[0]=mysql/idgen-schema.sql
```

#### 5. DefaultUidGenerator 使用

- 业务层（application）

```java
  @Resource
  protected CachedUidGenerator uidGenerator;
```

> 注意：CommCmd 默认已经注入！

- 门面层（facade）

```java
  CachedUidGenerator uidGenerator = (CachedUidGenerator)SpringContextHolder.getBean("uidGenerator");
```

## BidGenerator

BidGenerator 为了满足业务编码有可读性、全局唯一性、有序递增（甚至连续）、高性能等相关需求，支持基于DB和Redis两种生成可选模式，通过业务分割结合预分段方式来大幅度提升性能。

### 使用

#### 1. 引入依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.idgen-starter</artifactId>
  <version>2.0.0</version>
</dependency>
```

#### 2. 开启自动装配

```yml
xcan:
  idgen:
    enabled: true
  # Redis 模式时必须  
  redis:
    enabled: true
```

#### 3. 配置数据源和Redis

参见：sample_library 或 AngusTester 配置。

#### 4. 在数据库表 id_config 中配置 Bid

- 4.1. 参数说明

    - bizKey: 生成业务编码（bid）对应业务标识。
    - format: 编码格式：PREFIX_DATE_SEQ - 固定前缀+日期+序号, PREFIX_SEQ - 前缀+序号, DATE_SEQ -
      日期+序号, SEQ - 序号。
    - prefix: 编码前缀，支持长度 1-4 位。
    - dateFormat: 编码日期，YYYY - 年, YYYYMM - 年月, YYYYMMDD - 年月日。
    - seqLength: 序号长度，最大 40 位，建议长度 8-12 位，具体长度根据业务数据来判断决定。设定长度大于 0
      且当前ID值不足设定长度时自动左补 0，如：T2021090100000001，长度设定小于等于 0
      时，不固定长度自增。`注意：设定长度意味着生成ID为固定长度，设定长度小于等于0意味着ID变长。`
    - mode: 生成模式：REDIS - 基于Redis生成, DB - 基于数据库生成。
    - scope: 唯一性范围：PLATFORM - 平台唯一, TENANT - 租户下唯一。
    - tenantId: 编码所属租户，租户范围时只配置一条模版数据（对应租户ID -1），ID生成器会根据租户自动生成配置。
    - maxId: 数据库模式当前最大ID（以已分段缓存）。
    - step: 分段步长，允许最大 1000000，建议 1000 -
      10000，分段太小会增加DB和Redis压力，DB模式时甚至导致代码瓶颈，过大重启后会导致未使用分段丢失，即失去太大断号。

**注意：**

1. 表 id_config 不存在时会被自动创建！
2. 序列号建议长度8-12位，分段步长 1000 - 10000。

- 4.2 配置示例

```sql
-- SEQ格式+Redis模式+平台下唯一
INSERT INTO `id_config`(`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`, `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `last_modified_date`)
VALUES ('620d069a-c074-4b0e-9576-da2ed48092b2', 'biKey1002', 'SEQ', '', '', 8, 'REDIS', 'PLATFORM', -1, 0, 10000, '2021-09-03 19:01:40', '2021-09-03 19:01:40');
```

#### 5. BidGenerator注入示例

- 业务层（application）

```java

@Resource
BidGenerator bidGenerator;
```

- 门面层（facade）

```java
BidGenerator bidGenerator = (BidGenerator) SpringContextHolder.getBean("bidGenerator");
```

#### 6. BidGenerator API使用示例

```java
/**
 * 根据业务Key获取单个ID
 */
String getId(String bizKey);

/**
 * 根据业务Key和租户单个ID
 */
String getId(String bizKey, Long tenantId);

/**
 * 根据业务Key和租户ID获取batchNum个ID
 */
List<String> getIds(String bizKey, int batchNum);

/**
 * 根据业务Key和租户ID获取batchNum个ID
 */
List<String> getIds(String bizKey, int batchNum, Long tenantId);
```

## 性能结果

- [UidGenerator 性能](docs/UIDPerformance_zh.md)
- [BidGenerator 性能](docs/BIDPerformance_zh.md)
