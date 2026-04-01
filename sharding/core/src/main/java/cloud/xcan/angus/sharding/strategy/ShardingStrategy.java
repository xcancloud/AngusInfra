package cloud.xcan.angus.sharding.strategy;

/**
 * Strategy interface for computing the database shard index and table secondary index
 * from a given shard key value.
 *
 * <p>Implementations should be stateless and thread-safe.
 */
public interface ShardingStrategy {

  /**
   * Compute the database shard index for the given key.
   *
   * @param shardKey     the shard key value (e.g. tenant ID)
   * @param shardDbCount total number of database shards
   * @return a value in {@code [0, shardDbCount)}
   */
  int computeDbIndex(long shardKey, int shardDbCount);

  /**
   * Compute the table secondary index for the given key.
   *
   * @param tableKey       the table key value
   * @param shardTableCount total number of table shards
   * @return a value in {@code [0, shardTableCount)}, or {@code -1} if secondary indexing is disabled
   */
  int computeTableIndex(long tableKey, int shardTableCount);
}
