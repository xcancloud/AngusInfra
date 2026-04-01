-- ===========================================================================
-- Sharding Schema (PostgreSQL)
-- ===========================================================================

CREATE TABLE IF NOT EXISTS angus_shard_table
(
    table_name  VARCHAR(255) NOT NULL,
    shard_key   BIGINT       NOT NULL,
    db_index    INT          NOT NULL,
    table_index BIGINT       NOT NULL DEFAULT -1,
    CONSTRAINT pk_angus_shard_table PRIMARY KEY (table_name)
);

COMMENT ON TABLE angus_shard_table IS 'Registry of physically-created shard tables';
COMMENT ON COLUMN angus_shard_table.table_name IS 'Fully-qualified sharded table name, e.g. exec_sample-100-3';
COMMENT ON COLUMN angus_shard_table.shard_key IS 'Shard key used to determine the target DB shard';
COMMENT ON COLUMN angus_shard_table.db_index IS 'Zero-based DB shard index';
COMMENT ON COLUMN angus_shard_table.table_index IS 'Secondary table index; -1 indicates no secondary index';

CREATE INDEX IF NOT EXISTS idx_ast_shard_key ON angus_shard_table (shard_key);
CREATE INDEX IF NOT EXISTS idx_ast_db_index ON angus_shard_table (db_index);
