-- ===========================================================================
-- Sharding Schema (MySQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS angus_shard_table
(
    table_name
    VARCHAR
(
    255
) NOT NULL COMMENT 'Fully-qualified sharded table name, e.g. exec_sample-100-3',
    shard_key BIGINT NOT NULL COMMENT 'Shard key used to determine the target DB shard',
    db_index INT NOT NULL COMMENT 'Zero-based DB shard index',
    table_index BIGINT NOT NULL DEFAULT -1 COMMENT 'Secondary table index; -1 indicates no secondary index',
    PRIMARY KEY
(
    table_name
),
    INDEX idx_ast_shard_key
(
    shard_key
),
    INDEX idx_ast_db_index
(
    db_index
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = 'Registry of physically-created shard tables';
