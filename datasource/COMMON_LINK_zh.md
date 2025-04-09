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
-- GM -----
-- UC ------------------------------
GRANT select ON `xcan_gm`.`tenant` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`user0` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`dept` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`dept_user` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`group0` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`group_user` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`org_tag` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`org_tag_target` TO `commonlink`@`%`;
-- AAS ------------------------------
GRANT select ON `xcan_gm`.`service` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`api` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`app` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`auth_user` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`auth_policy` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`to_policy` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`to_user` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`to_policy_user` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`app_open` TO `commonlink`@`%`;
-- WPUSH ------------------------------
GRANT select ON `xcan_gm`.`mcenter_online` TO `commonlink`@`%`;
-- COMMON ------------------------------
GRANT select ON `xcan_gm`.`c_i18n_messages` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`c_setting` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`c_setting_tenant` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`c_setting_tenant_quota` TO `commonlink`@`%`;
GRANT select ON `xcan_gm`.`c_setting_user` TO `commonlink`@`%`;
FLUSH PRIVILEGES;
-- STORE -----------------------------------
GRANT select ON `xcan_store`.`goods` TO `commonlink`@`%`;
GRANT select ON `xcan_store`.`store_goods` TO `commonlink`@`%`;
FLUSH PRIVILEGES;
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
-- STORE 库
CREATE
SERVER xcan_store_link
FOREIGN DATA WRAPPER mysql
OPTIONS (USER 'commonlink', PASSWORD 'LUYSMvzdVR0', HOST 'dev-mw.xcan.cloud', PORT 3306, DATABASE 'xcan_store');
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

-- `address` json -> `address` varchar(200) 
CREATE TABLE `user0` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键ID',
  `username` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户名',
  `first_name` varchar(100) COLLATE utf8mb4_bin DEFAULT '' COMMENT '名字',
  `last_name` varchar(100) COLLATE utf8mb4_bin DEFAULT '' COMMENT '姓',
  `fullname` varchar(100) COLLATE utf8mb4_bin DEFAULT '' COMMENT '全名',
  `itc` varchar(8) COLLATE utf8mb4_bin DEFAULT '' COMMENT '国际电话区号',
  `country` varchar(16) COLLATE utf8mb4_bin DEFAULT '' COMMENT '国家编码',
  `email` varchar(100) COLLATE utf8mb4_bin DEFAULT '' COMMENT '注册邮箱',
  `mobile` varchar(16) COLLATE utf8mb4_bin DEFAULT '' COMMENT '注册手机号',
  `signup_account_type` varchar(16) COLLATE utf8mb4_bin DEFAULT 'NOOP' COMMENT '注册账号类型：MOBILE-手机号；EMAIL-邮箱；NOOP=未操作',
  `signup_account` varchar(100) COLLATE utf8mb4_bin DEFAULT '' COMMENT '租户注册账号（手机号或邮箱）',
  `landline` varchar(40) COLLATE utf8mb4_bin DEFAULT '' COMMENT '座机',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '所属租户ID',
  `tenant_name` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '租户名称',
  `avatar` varchar(400) COLLATE utf8mb4_bin DEFAULT '' COMMENT '用户头像地址',
  `title` varchar(100) COLLATE utf8mb4_bin DEFAULT '' COMMENT '职务',
  `gender` varchar(10) COLLATE utf8mb4_bin DEFAULT '' COMMENT '性别：MALE-男；FEMALE-女；UNKNOWN-未知；',
  `address` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '联系地址',
  `source` varchar(40) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户来源：PLAT_REGISTER-平台注册；BACK_ADD-后台添加；THIRD_PARTY_LOGIN-三方登录; LDAP_SYNCHRONIZE-Ldap同步',
  `directory_id` bigint(20) DEFAULT NULL COMMENT '用户目录ID',
  `main_dept_id` bigint(20) DEFAULT '-1' COMMENT '主部门ID',
  `dept_head` int(11) DEFAULT '0' COMMENT '是否部门负责人：0-一般用户；1-部门负责人；',
  `sys_admin` int(11) NOT NULL DEFAULT '0' COMMENT '是否系统管理员：0-一般用户；1-系统管理员；',
  `expired` int(11) NOT NULL COMMENT '到期标记：0-未到期；1-已到期',
  `expired_date` datetime DEFAULT NULL COMMENT '到期时间',
  `enabled` int(11) NOT NULL DEFAULT '0' COMMENT '用户状态：0-禁用；1-启用',
  `disable_reason` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '禁用原因',
  `deleted` int(11) NOT NULL DEFAULT '0' COMMENT '删除状态：0-未删除；1-已删除',
  `locked` int(11) NOT NULL COMMENT '锁定状态：0-未锁定；1-已锁定',
  `last_lock_date` datetime DEFAULT NULL COMMENT '最后锁定时间',
  `lock_start_date` datetime DEFAULT NULL COMMENT '锁定开始时间',
  `lock_end_date` datetime DEFAULT NULL COMMENT '锁定结束时间',
  `last_modified_passd_date` datetime DEFAULT NULL COMMENT '最后修改密码的时间',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_username` (`username`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE,
  KEY `uidx_tenantId_email` (`tenant_id`,`email`) USING BTREE,
  KEY `uidx_tenantId_mobile` (`tenant_id`,`mobile`) USING BTREE,
  KEY `idx_mobile` (`mobile`) USING BTREE,
  KEY `idx_source` (`source`) USING BTREE,
  KEY `idx_gender` (`gender`) USING BTREE,
  KEY `idx_admin_flag` (`sys_admin`) USING BTREE,
  KEY `idx_enabled` (`enabled`) USING BTREE,
  KEY `idx_locked` (`locked`) USING BTREE,
  KEY `idx_lock_start_date` (`lock_start_date`) USING BTREE,
  KEY `idx_lock_end_date` (`lock_end_date`) USING BTREE,
  KEY `idx_fullname` (`fullname`) USING BTREE
  -- FULLTEXT KEY `fx_name_tag_value` (`fullname`,`mobile`,`title`,`username`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='用户' CONNECTION='xcan_gm_link/user0';

CREATE TABLE `dept` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `code` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '部门编码',
  `parent_like_id` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '部门系统ID：所以父部门ID符号“-”相连',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '机构名称',
  `pid` bigint(20) NOT NULL DEFAULT '-1' COMMENT '上级机构ID',
  `level` int(11) NOT NULL COMMENT '部分层级',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_pid` (`pid`) USING BTREE,
  KEY `idx_parent_like_id` (`parent_like_id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_level` (`level`) USING BTREE
  -- FULLTEXT KEY `fx_name_code` (`code`,`name`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='部门'  CONNECTION = 'xcan_gm_link/dept';

CREATE TABLE `dept_user` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键ID',
  `dept_id` bigint(100) NOT NULL DEFAULT '-1' COMMENT '部门编码',
  `user_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '用户id',
  `main_dept` int(11) NOT NULL COMMENT '是否主部门',
  `dept_head` int(11) NOT NULL DEFAULT '0' COMMENT '是否部门负责人：0-一般用户；1-部门负责人；',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_dept_id` (`dept_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `uidx_dept_user_id` (`dept_id`,`user_id`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='部门用户关系表'  CONNECTION = 'xcan_gm_link/dept_user';

CREATE TABLE `group0` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '编码',
  `enabled` int(11) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `source` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户组来源：BACK_ADD-后台添加； LDAP_SYNCHRONIZE-Ldap同步',
  `directory_id` bigint(20) DEFAULT NULL COMMENT '用户目录ID',
  `directory_gid_number` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户目录组成员ID',
  `remark` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '备注',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID，默认-1表示未关联租户，当用户自定义角色时必须',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_tenant_id_code` (`tenant_id`,`code`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_create_date` (`created_date`) USING BTREE,
  KEY `idx_source` (`source`) USING BTREE
  -- FULLTEXT KEY `fx_name_tag_value` (`name`,`code`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户组' CONNECTION='xcan_gm_link/group0';

CREATE TABLE `group_user` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键ID',
  `group_id` bigint(20) NOT NULL COMMENT '用户组ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_group_user_id` (`group_id`,`user_id`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户组用户关联表' CONNECTION='xcan_gm_link/group_user';

CREATE TABLE `org_tag` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '标签名称',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '所属租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_tenant_id_name` (`name`,`tenant_id`) USING BTREE,
  KEY `idx_create_date` (`created_date`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
  -- FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='标签' CONNECTION = 'xcan_gm_link/org_tag';

CREATE TABLE `org_tag_target` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `target_type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '目标类型：部门、组、用户',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_tag_target_id` (`tag_id`,`target_id`) USING BTREE,
  KEY `idx_tag_id` (`tag_id`) USING BTREE,
  KEY `idx_target_id` (`target_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_target_type` (`target_type`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='标签对象' CONNECTION = 'xcan_gm_link/org_tag_target';

-- AAS 库
CREATE TABLE `service` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '编码',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `description` varchar(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '描述',
  `source` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '来源：BACK_ADD、EUREKA、NOCAS、CONSUL',
  `enabled` int(11) NOT NULL COMMENT '有效状态',
  `api_num` int(11) NOT NULL DEFAULT '0' COMMENT '接口数',
  `route_path` varchar(80) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '网关路由路径',
  `url` varchar(400) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '服务地址',
  `health_url` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '健康检查地址',
  `api_doc_url` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT 'API文档地址',
  `created_by` bigint(21) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(21) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_code` (`code`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_summary_group` (`created_date`,`source`,`enabled`) USING BTREE
  -- FULLTEXT KEY `fx_name_code_description` (`name`,`code`,`description`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='服务表' CONNECTION='xcan_gm_link/service';

CREATE TABLE `api` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `code` varchar(160) COLLATE utf8mb4_bin NOT NULL COMMENT '编码',
  `uri` varchar(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '接口地址',
  `method` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '请求方法',
  `type` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '接口类型(ApiType)',
  `description` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '描述',
  `resource_name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '资源名',
  `resource_desc` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '资源描述',
  `enabled` int(11) NOT NULL COMMENT '有效状态',
  `service_id` bigint(20) NOT NULL COMMENT '服务ID',
  `service_code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '所属服务编码\n',
  `service_name` varchar(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '所属服务名称',
  `service_enabled` int(11) DEFAULT NULL COMMENT '服务启用状态',
  `sync` int(11) NOT NULL COMMENT '是否是同步',
  `swagger_deleted` int(11) DEFAULT NULL COMMENT '同步删除状态',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_service_code` (`code`,`service_code`) USING BTREE,
  KEY `idx_service_name` (`service_name`) USING BTREE,
  KEY `uidx_name` (`name`) USING BTREE,
  KEY `idx_service_code` (`service_code`) USING BTREE
  -- FULLTEXT KEY `fx_name_code_service_name_description` (`name`,`code`,`service_name`,`description`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口表' CONNECTION='xcan_gm_link/api';

-- delete api_data 
CREATE TABLE `app` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `code` varchar(80) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '编码',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `show_name` varchar(40) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '展示名称',
  `icon` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '图标',
  `type` varchar(10) COLLATE utf8mb4_bin NOT NULL COMMENT '应用类型：CLOUD_APP、BASE_APP、OP_APP',
  `edition_type` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '版本类型',
  `description` varchar(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '描述',
  `auth_ctrl` int(11) DEFAULT NULL COMMENT '授权控制标志态：0-不控制；1-控制',
  `enabled` int(11) NOT NULL COMMENT '启用状态：0-禁用；1-启用',
  `url` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT 'URL',
  `sequence` int(11) NOT NULL DEFAULT '1' COMMENT '序号，值越小越靠前',
   -- `api_ids` varchar(800) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '关联接口编码',
  `version` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '应用版本：major(主版本号)、minor(次版本号)、patch(修订号)',
  `open_stage` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '开通阶段：(SINUP)注册、AUTH_PASSED(实名认证通过户) 、OPEN_SUCCESS(开通成功)',
  `client_id` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '所属端ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_enabled_fl_id_sequence` (`enabled`,`id`,`sequence`) USING BTREE,
  KEY `uidx_name_code` (`name`,`code`) USING BTREE,
  KEY `uidx_show_name_name` (`show_name`,`code`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE
  -- FULLTEXT KEY `fx_name_code_show_name_description` (`name`,`code`,`show_name`,`description`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='应用表' CONNECTION='xcan_gm_link/app';

CREATE TABLE `auth_user` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `passd` varchar(160) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户密码',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `tenant_real_name_status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '是否实名认证(0未认证 1已认证)',
  `to_user_flag` int(11) NOT NULL DEFAULT '0' COMMENT '是否运营用户标志',
  `passd_strength` varchar(10) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码强度：WEAK-弱，MEDIUM-中，STRONG-强',
  `passd_expired` int(11) NOT NULL COMMENT '密码到期标记：0-未到期；1-已到期',
  `passd_expired_date` datetime DEFAULT NULL COMMENT '密码到期时间',
  `last_modified_passd_date` datetime DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改密码的时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_tenant_real_name_status` (`tenant_real_name_status`) USING BTREE,
  KEY `idx_passd_expired_date` (`passd_expired_date`) USING BTREE,
  KEY `idx_passd_expired` (`passd_expired`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户' CONNECTION='xcan_gm_link/auth_user';

CREATE TABLE `auth_policy` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `code` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '编码，规则名称英文蛇形表示',
  `enabled` int(11) NOT NULL COMMENT '有效状态：0-禁用；1-启用',
  `type` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '1' COMMENT '策略分类',
  `default0` int(11) NOT NULL COMMENT '默认权限策略',
  `grant_stage` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '-1' COMMENT '授权阶段',
  `description` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '描述',
  `app_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '所属应用ID',
  `client_id` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '应用所属端',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_code` (`code`,`tenant_id`) USING BTREE,
  UNIQUE KEY `uidx_name_app_id` (`name`,`tenant_id`,`app_id`) USING BTREE,
  KEY `idx_app_id` (`app_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_enabled` (`enabled`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_client_id` (`client_id`) USING BTREE
  -- FULLTEXT KEY `fx_code_name_descroption` (`code`,`name`,`description`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='授权策略' CONNECTION='xcan_gm_link/auth_policy';

CREATE TABLE `to_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '编码，规则名称英文蛇形表示',
  `enabled` int(11) NOT NULL COMMENT '有效状态：0-禁用；1-启用',
  `description` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '描述',
  `app_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '所属应用ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_code` (`code`) USING BTREE,
  UNIQUE KEY `uidx_name` (`name`) USING BTREE,
  KEY `idx_app_id` (`app_id`) USING BTREE
  -- FULLTEXT KEY `fx_name_code` (`name`,`code`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='运营策略（租户运营角色）' CONNECTION='xcan_gm_link/to_policy';

CREATE TABLE `to_user` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uid_user_id` (`user_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='策略用户关联表' CONNECTION='xcan_gm_link/to_user';

CREATE TABLE `to_policy_user` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `to_policy_id` bigint(20) NOT NULL COMMENT '租户运营策略ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_policy_user` (`to_policy_id`,`user_id`) USING BTREE,
  KEY `idx_policy_id` (`to_policy_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='策略用户关联表' CONNECTION='xcan_gm_link/to_policy_user';

CREATE TABLE `app_open` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_id` bigint(20) NOT NULL COMMENT '开通应用ID',
  `app_code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '开通应用编码',
  `app_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '应用类型',
  `version` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '开通版本版本',
  `client_id` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '应用端ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '开通租户ID',
  `user_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '开通用户ID',
  `open_date` datetime NOT NULL COMMENT '开通时间',
  `expiration_date` datetime NOT NULL COMMENT '到期时间',
  `expiration_deleted` int(11) NOT NULL COMMENT '过期删除标志',
  `op_client_open` int(11) NOT NULL COMMENT '运营端开通标志',
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_expiration_date` (`expiration_date`) USING BTREE,
  KEY `idx_appcode_version_tenant_id` (`app_code`,`version`,`tenant_id`) USING BTREE,
  KEY `idx_expiration_deleted` (`expiration_deleted`) USING BTREE,
  KEY `idx_op_client_open` (`op_client_open`) USING BTREE,
  KEY `idx_app_id` (`app_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_client_id` (`client_id`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='应用开通表' CONNECTION='xcan_gm_link/app_open';

-- WPUSH 库
CREATE TABLE `mcenter_online` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `fullname` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '用户姓名',
  `user_agent` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '用户终端',
  `device_id` varchar(160) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '设备ID',
  `remote_address` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '访问ID地址',
  `online_date` datetime DEFAULT NULL COMMENT '上次上线时间',
  `offline_date` datetime DEFAULT NULL COMMENT '上次下线时间',
  `online` int(11) NOT NULL COMMENT '是否在线：1-在线；0-未在线',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `uidx_user_id` (`user_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
  -- FULLTEXT KEY `fx_fullname_remote_address` (`fullname`,`remote_address`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='运营工单聊天消息' CONNECTION='xcan_gm_link/mcenter_online';

-- COMMON 库
CREATE TABLE `c_i18n_messages` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `type` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '分类',
  `language` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '语言',
  `default_message` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '默认消息',
  `i18n_message` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '国际化消息',
  `private0` int(11) NOT NULL COMMENT '是否私有化',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_key_language_default_message` (`type`,`language`,`default_message`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_language` (`language`) USING BTREE,
  KEY `idx_default_message` (`default_message`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='国际化资源表' CONNECTION='xcan_gm_link/c_i18n_messages';

CREATE TABLE `c_setting` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `key` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '配置参数key',
  `value` varchar(16000) COLLATE utf8mb4_bin NOT NULL COMMENT '配置参数值',
  `global_default` int(11) NOT NULL COMMENT '是否全局默认值标志',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_key` (`key`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='配置表' CONNECTION='xcan_gm_link/c_setting';

-- json -> varchar
CREATE TABLE `c_setting_tenant` (
 `id` bigint(20) NOT NULL COMMENT '主键ID',
 `invitation_code` varchar(80) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '邀请注册码，检索冗余',
 `locale_data` varchar(200) DEFAULT NULL COMMENT '租户国际化设置',
 `theme_data` varchar(2000) DEFAULT NULL COMMENT '租户外观设置',
 `func_data` varchar(500) DEFAULT NULL COMMENT '租户平台默认功能指标',
 `perf_data` varchar(500) DEFAULT NULL COMMENT '租户平台默认性能指标',
 `stability_data` varchar(500) DEFAULT NULL COMMENT '租户平台默认稳定性指标',
 `security_data` varchar(500) DEFAULT NULL COMMENT '租户账号安全设置',
 `server_api_proxy_data` varchar(500) DEFAULT NULL COMMENT '服务端Api 代理配置',
 `tester_event_data` varchar(500) DEFAULT NULL COMMENT 'AngusTester事件通知类型配置',
 `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
 PRIMARY KEY (`id`) USING BTREE,
 UNIQUE KEY `uidx_tenant_id` (`tenant_id`) USING BTREE,
 UNIQUE KEY `uidx_invitation_code` (`invitation_code`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='租户设置' CONNECTION='xcan_gm_link/c_setting_tenant';

CREATE TABLE `c_setting_tenant_quota` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `app_code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '所属应用编码',
  `service_code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '所属服务编码',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '资源名称',
  `allow_change` int(11) NOT NULL COMMENT '是否允许修改配额',
  `lcs_ctrl` int(11) NOT NULL COMMENT 'LCS控制标识',
  `calc_remaining` int(11) NOT NULL COMMENT '计算剩余配额标志',
  `quota` bigint(20) NOT NULL COMMENT '当前生效配额值',
  `min` bigint(20) NOT NULL DEFAULT '0' COMMENT '最小允许配置值',
  `max` bigint(20) NOT NULL DEFAULT '0' COMMENT '最大允许配置值',
  `capacity` bigint(20) NOT NULL COMMENT '总容量（所有租户上线）',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_tenant_id_name` (`tenant_id`,`name`) USING BTREE,
  KEY `idx_app_code` (`app_code`) USING BTREE,
  KEY `idx_service_code` (`service_code`) USING BTREE,
  KEY `idx_resource` (`name`) USING BTREE,
  KEY `idx_quota` (`quota`) USING BTREE,
  KEY `idx_allow_change` (`allow_change`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
  -- FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='租户配额设置' CONNECTION='xcan_gm_link/c_setting_tenant_quota';

-- json -> varchar
CREATE TABLE `c_setting_user` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键ID',
  `preference` varchar(500) DEFAULT NULL COMMENT '偏好设置',
  `api_proxy` varchar(500) DEFAULT NULL COMMENT '用户代理API配置',
  `social_bind` varchar(500) DEFAULT NULL COMMENT '三方登录用户绑定信息',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户' CONNECTION='xcan_gm_link/c_setting_user';

-- STORE 库
CREATE TABLE `goods` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `no` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '商品编号',
  `edition_type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '版本类型',
  `type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '产品类型',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '产品名称',
  `code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '产品编码',
  `version` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '产品版本',
  `signature` varchar(800) COLLATE utf8mb4_bin DEFAULT '' COMMENT '商品核心制品包SHA512签名',
  `signature_artifact` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '商品核心制品包',
  `package_id` bigint(20) DEFAULT NULL COMMENT '安装包ID',
  `allow_release_flag` int(11) NOT NULL COMMENT '允许发布标志',
  `online` int(11) NOT NULL COMMENT '上架标志',
  `charge_flag` int(11) NOT NULL COMMENT '收费标记',
  `lcs_protection_flag` int(11) NOT NULL COMMENT '是否许可保护',
  `apply_edition_type` varchar(80) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '适用应用版本类型：云服务版(CLOUD_SERVICE)/数据中心版(DATACENTER)/企业版(ENTERPRISE)/社区版(COMMUNITY)',
  `apply_app_code` varchar(160) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '适用应用编码',
  `apply_version` varchar(160) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '适用应用版本范围：最小版本号、最大值版本号',
  `downward_version` varchar(160) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '向下兼容版本范围：最小版本号、最大值版本号',
  `deleted` int(11) NOT NULL COMMENT '删除标志',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_code_version_type_version` (`code`,`edition_type`,`version`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_release_flag` (`allow_release_flag`) USING BTREE,
  KEY `idx_online` (`online`) USING BTREE,
  KEY `idx_charge_flag` (`charge_flag`) USING BTREE,
  KEY `idx_package_id` (`package_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_apply_service_code` (`apply_app_code`) USING BTREE
  -- FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='商品表' CONNECTION='xcan_store_link/goods';

CREATE TABLE `store_goods` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `goods_id` bigint(20) NOT NULL COMMENT '商品ID',
  `edition_type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '版本类型',
  `type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '商品类型',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '商品名称',
  `code` varchar(80) COLLATE utf8mb4_bin NOT NULL COMMENT '商品编号',
  `version` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '商品版本',
  `apply_edition_type` varchar(80) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '适用应用版本类型：云服务版(CLOUD_SERVICE)/数据中心版(DATACENTER)/企业版(ENTERPRISE)/社区版(COMMUNITY)',
  `charge_flag` int(11) NOT NULL COMMENT '收费标记',
  `icon_url` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '商品ICON',
  `pricing_url` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '定价地址',
  `pricing_template_id` bigint(20) DEFAULT NULL COMMENT '定价模版ID',
  `introduction` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '商品介绍',
  -- `banner_url` varchar(1600) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品介绍Banner地址',
  -- `information` varchar(6000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `lcs_protection_flag` int(11) NOT NULL COMMENT '是否许可保护',
  -- `features` varchar(3200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '升级功能介绍',
  -- `videos` varchar(1800) COLLATE utf8mb4_bin NOT NULL COMMENT '介绍视频',
  `allow_comment_flag` int(11) NOT NULL COMMENT '是否支持评论',
  `comment_num` int(11) NOT NULL COMMENT '评论数',
  `hot_flag` int(11) NOT NULL COMMENT '是否热门',
  `star_num` int(11) NOT NULL DEFAULT '0' COMMENT '点赞数',
  `online_status` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '上架状态',
  `online_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '上架人ID',
  `online_date` datetime DEFAULT NULL COMMENT '上架时间',
  `offline_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '下架人',
  `offline_date` datetime DEFAULT NULL COMMENT '下架时间',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_code_version_edition` (`code`,`version`,`edition_type`) USING BTREE,
  UNIQUE KEY `uidx_goods_id` (`goods_id`) USING BTREE,
  KEY `idx_charge_flag` (`charge_flag`) USING BTREE,
  KEY `idx_edition_type` (`edition_type`) USING BTREE,
  KEY `idx_product_type` (`type`) USING BTREE,
  KEY `idx_hot_flag` (`hot_flag`) USING BTREE,
  KEY `idx_online_status` (`online_status`) USING BTREE,
  KEY `idx_comment_num` (`comment_num`) USING BTREE,
  KEY `idx_star_num` (`star_num`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE
  -- FULLTEXT KEY `fx_name_code_intro_info` (`name`,`code`,`introduction`,`information`,`apply_edition_type`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=FEDERATED DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='商店商品表' CONNECTION='xcan_store_link/store_goods';
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
