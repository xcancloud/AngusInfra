CREATE TABLE IF NOT EXISTS test_shard_table (
  id        BIGINT      NOT NULL,
  shard_key BIGINT      NOT NULL,
  payload   VARCHAR(255),
  CONSTRAINT pk_test_shard_table PRIMARY KEY (id)
);
CREATE INDEX idx_test_shard_table_shard_key ON test_shard_table (shard_key);
