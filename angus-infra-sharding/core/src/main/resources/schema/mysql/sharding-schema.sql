-- ===========================================================================
-- Sharding Schema (MySQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS angus_shard_table (
    table_name  VARCHAR(255) NOT NULL,
    shard_key   BIGINT       NOT NULL,
    db_index    INT          NOT NULL,
    table_index BIGINT       NOT NULL DEFAULT -1,
    PRIMARY KEY (table_name),
    INDEX idx_ast_shard_key (shard_key),
    INDEX idx_ast_db_index (db_index)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
