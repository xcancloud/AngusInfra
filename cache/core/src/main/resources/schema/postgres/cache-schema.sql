-- ===========================================================================
-- Cache Schema (PostgreSQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS angus_cache_entries (
    id BIGSERIAL PRIMARY KEY,
    cache_key VARCHAR(256) NOT NULL,
    cache_value TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    expire_at TIMESTAMP,
    ttl_seconds BIGINT,
    is_expired BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_cache_key UNIQUE (cache_key)
);
CREATE INDEX IF NOT EXISTS idx_expire_time ON angus_cache_entries (expire_at);
