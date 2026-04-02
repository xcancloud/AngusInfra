-- ===========================================================================
-- Queue Schema (MySQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS angus_mq_message (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    topic           VARCHAR(128)  NOT NULL,
    partition_id    INT           NOT NULL,
    priority        INT           NOT NULL DEFAULT 0,
    payload         JSON          NOT NULL,
    headers         JSON,
    status          TINYINT       NOT NULL DEFAULT 0,
    visible_at      DATETIME(6)   NOT NULL,
    lease_until     DATETIME(6),
    lease_owner     VARCHAR(128),
    attempts        INT           NOT NULL DEFAULT 0,
    max_attempts    INT           NOT NULL DEFAULT 16,
    idempotency_key VARCHAR(256),
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    version         BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_mq_msg_topic_status_visible (topic, status, visible_at),
    INDEX idx_mq_msg_status_lease_until (status, lease_until),
    INDEX idx_mq_msg_lease_owner (lease_owner),
    INDEX idx_mq_msg_attempts (attempts)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS angus_mq_dead_letter (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    topic        VARCHAR(128) NOT NULL,
    partition_id INT          NOT NULL,
    payload      JSON         NOT NULL,
    headers      JSON,
    attempts     INT          NOT NULL,
    reason       VARCHAR(256),
    created_at   DATETIME(6)  NOT NULL,
    deleted_at   DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_mq_dlq_topic (topic),
    INDEX idx_mq_dlq_deleted_at (deleted_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
