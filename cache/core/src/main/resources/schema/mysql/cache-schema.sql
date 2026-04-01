-- ===========================================================================
-- Cache Schema (MySQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS angus_cache_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cache_key VARCHAR(256) NOT NULL,
    cache_value LONGTEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    expire_at DATETIME,
    ttl_seconds BIGINT,
    is_expired BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_cache_key (cache_key),
    INDEX idx_expire_time (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
