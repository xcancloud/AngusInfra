-- ===========================================================================
-- Job Scheduler Schema
-- Supports MySQL 8 / H2 / PostgreSQL (with minor dialect adjustments)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS scheduled_job (
    id                 BIGINT          NOT NULL AUTO_INCREMENT,
    job_name           VARCHAR(255)    NOT NULL,
    job_group          VARCHAR(255)    NOT NULL,
    cron_expression    VARCHAR(255)    NOT NULL,
    bean_name          VARCHAR(255)    NOT NULL,
    job_type           VARCHAR(50)     NOT NULL,
    status             VARCHAR(50)     NOT NULL,
    sharding_count     INT             DEFAULT 1,
    sharding_parameter TEXT,
    retry_count        INT             DEFAULT 0,
    max_retry_count    INT             DEFAULT 3,
    description        TEXT,
    last_execute_time  DATETIME,
    next_execute_time  DATETIME,
    create_time        DATETIME        NOT NULL,
    update_time        DATETIME        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_name_group (job_name, job_group),
    INDEX idx_sj_status (status),
    INDEX idx_sj_next_execute_time (next_execute_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS job_execution_log (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    job_id         BIGINT      NOT NULL,
    job_name       VARCHAR(255) NOT NULL,
    sharding_item  INT,
    status         VARCHAR(50) NOT NULL,
    start_time     DATETIME    NOT NULL,
    end_time       DATETIME,
    execution_time BIGINT,
    result         TEXT,
    error_message  TEXT,
    executor_node  VARCHAR(255),
    PRIMARY KEY (id),
    INDEX idx_jel_job_id (job_id),
    INDEX idx_jel_start_time (start_time),
    INDEX idx_jel_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS distributed_lock (
    lock_key     VARCHAR(255) NOT NULL,
    lock_value   VARCHAR(255) NOT NULL,
    owner        VARCHAR(255) NOT NULL,
    acquire_time DATETIME     NOT NULL,
    expire_time  DATETIME     NOT NULL,
    version      BIGINT       DEFAULT 0,
    PRIMARY KEY (lock_key),
    INDEX idx_dl_expire_time (expire_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS job_shard (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    job_id             BIGINT      NOT NULL,
    sharding_item      INT         NOT NULL,
    sharding_parameter VARCHAR(255),
    status             VARCHAR(50) NOT NULL,
    map_result         TEXT,
    executor_node      VARCHAR(255),
    start_time         DATETIME,
    end_time           DATETIME,
    PRIMARY KEY (id),
    INDEX idx_js_job_id (job_id),
    INDEX idx_js_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
