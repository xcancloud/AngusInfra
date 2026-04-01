-- ===========================================================================
-- Job Scheduler Schema (PostgreSQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS scheduled_job (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL,
    job_group VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(255) NOT NULL,
    bean_name VARCHAR(255) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    sharding_count INT DEFAULT 1,
    sharding_parameter TEXT,
    retry_count INT DEFAULT 0,
    max_retry_count INT DEFAULT 3,
    description TEXT,
    last_execute_time TIMESTAMP,
    next_execute_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT uk_job_name_group UNIQUE (job_name, job_group)
);
CREATE INDEX IF NOT EXISTS idx_sj_status_next_exec ON scheduled_job (status, next_execute_time);

CREATE TABLE IF NOT EXISTS job_execution_log (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    sharding_item INT,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    execution_time BIGINT,
    result TEXT,
    error_message TEXT,
    executor_node VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_jel_job_id ON job_execution_log (job_id);
CREATE INDEX IF NOT EXISTS idx_jel_start_time ON job_execution_log (start_time);
CREATE INDEX IF NOT EXISTS idx_jel_status ON job_execution_log (status);

CREATE TABLE IF NOT EXISTS distributed_lock (
    lock_key VARCHAR(255) PRIMARY KEY,
    lock_value VARCHAR(255) NOT NULL,
    owner VARCHAR(255) NOT NULL,
    acquire_time TIMESTAMP NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_dl_expire_time ON distributed_lock (expire_time);

CREATE TABLE IF NOT EXISTS job_shard (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    sharding_item INT NOT NULL,
    sharding_parameter VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    map_result TEXT,
    executor_node VARCHAR(255),
    start_time TIMESTAMP,
    end_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_js_job_id ON job_shard (job_id);
CREATE INDEX IF NOT EXISTS idx_js_status ON job_shard (status);
