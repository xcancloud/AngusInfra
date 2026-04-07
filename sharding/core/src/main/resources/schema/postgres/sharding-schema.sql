-- ===========================================================================
-- Sharding Schema (PostgreSQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS angus_shard_table (
    table_name  VARCHAR(255) NOT NULL,
    shard_key   BIGINT       NOT NULL,
    db_index    INT          NOT NULL,
    table_index BIGINT       NOT NULL DEFAULT -1,
    CONSTRAINT pk_angus_shard_table PRIMARY KEY (table_name)
);

CREATE INDEX IF NOT EXISTS idx_ast_shard_key ON angus_shard_table (shard_key);
CREATE INDEX IF NOT EXISTS idx_ast_db_index  ON angus_shard_table (db_index);
