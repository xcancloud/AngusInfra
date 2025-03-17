-- DROP TABLE IF EXISTS `instance`;
CREATE TABLE `instance`
(
    `pk`                 varchar(40)  NOT NULL,
    `id`                 bigint(21)   NOT NULL,
    `host`               varchar(160) NOT NULL DEFAULT '',
    `port`               varchar(40)  NOT NULL DEFAULT '',
    `instance_type`      varchar(40)  NOT NULL DEFAULT '',
    `create_date`        datetime    NOT NULL,
    `last_modified_date` datetime    NOT NULL,
    PRIMARY KEY (`pk`),
    UNIQUE KEY `uidx_host_port` (`host`, `port`) USING BTREE
) ENGINE = InnoDB;


-- DROP TABLE IF EXISTS `id_config`;
CREATE TABLE `id_config`
(
    `pk`                 varchar(40) NOT NULL,
    `biz_key`            varchar(80) NOT NULL,
    `format`             varchar(16) NOT NULL,
    `prefix`             varchar(4)  NOT NULL,
    `date_format`        varchar(8)           DEFAULT NULL,
    `seq_length`         int(11)     NOT NULL,
    `mode`               varchar(8)  NOT NULL,
    `scope`              varchar(16) NOT NULL,
    `tenant_id`          bigint(20)  NOT NULL DEFAULT '-1',
    `max_id`             bigint(20)  NOT NULL DEFAULT '0',
    `step`               bigint(20)  NOT NULL,
    `create_date`        datetime    NOT NULL,
    `last_modified_date` datetime    NOT NULL,
    PRIMARY KEY (`pk`) USING BTREE,
    UNIQUE INDEX `uidx_biz_key_tenant_id` (`biz_key`, `tenant_id`) USING BTREE,
    INDEX `uidx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE = InnoDB;
