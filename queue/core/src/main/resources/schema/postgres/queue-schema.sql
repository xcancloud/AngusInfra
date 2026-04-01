-- ===========================================================================
-- Queue Schema (PostgreSQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS angus_mq_message (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(128) NOT NULL,
    partition_id INT NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    payload JSONB NOT NULL,
    headers JSONB,
    status SMALLINT NOT NULL DEFAULT 0,
    visible_at TIMESTAMP(6) NOT NULL,
    lease_until TIMESTAMP(6),
    lease_owner VARCHAR(128),
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 16,
    idempotency_key VARCHAR(256),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_mq_msg_topic_status_visible
    ON angus_mq_message (topic, status, visible_at);
CREATE INDEX IF NOT EXISTS idx_mq_msg_status_lease_until
    ON angus_mq_message (status, lease_until);
CREATE INDEX IF NOT EXISTS idx_mq_msg_lease_owner
    ON angus_mq_message (lease_owner);
CREATE INDEX IF NOT EXISTS idx_mq_msg_attempts
    ON angus_mq_message (attempts);

CREATE TABLE IF NOT EXISTS angus_mq_dead_letter (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(128) NOT NULL,
    partition_id INT NOT NULL,
    payload JSONB NOT NULL,
    headers JSONB,
    attempts INT NOT NULL,
    reason VARCHAR(256),
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6)
);
CREATE INDEX IF NOT EXISTS idx_mq_dlq_topic ON angus_mq_dead_letter (topic);
CREATE INDEX IF NOT EXISTS idx_mq_dlq_deleted_at ON angus_mq_dead_letter (deleted_at);
