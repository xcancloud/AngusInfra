-- DROP TABLE IF EXISTS angus_instance;
CREATE TABLE angus_instance
(
    pk            varchar(40)  NOT NULL,
    id            bigint       NOT NULL,
    host          varchar(160) NOT NULL DEFAULT '',
    port          varchar(40)  NOT NULL DEFAULT '',
    instance_type varchar(40)  NOT NULL DEFAULT '',
    create_date   timestamp    NOT NULL,
    modified_date timestamp    NOT NULL,
    PRIMARY KEY (pk),
    CONSTRAINT uidx_host_port UNIQUE (host, port)
);

-- DROP TABLE IF EXISTS angus_id_config;
CREATE TABLE angus_id_config
(
    pk            varchar(40) NOT NULL,
    biz_key       varchar(80) NOT NULL,
    format        varchar(16) NOT NULL,
    prefix        varchar(4)  NOT NULL,
    date_format   varchar(8)           DEFAULT NULL,
    seq_length    integer     NOT NULL,
    mode          varchar(8)  NOT NULL,
    scope         varchar(16) NOT NULL,
    tenant_id     bigint      NOT NULL DEFAULT -1,
    max_id        bigint      NOT NULL DEFAULT 0,
    step          bigint      NOT NULL,
    create_date   timestamp   NOT NULL,
    modified_date timestamp   NOT NULL,
    PRIMARY KEY (pk),
    CONSTRAINT uidx_biz_key_tenant_id UNIQUE (biz_key, tenant_id)
);
CREATE INDEX uidx_tenant_id ON angus_id_config (tenant_id);
