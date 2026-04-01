package cloud.xcan.angus.sharding.strategy;

/**
 * Default modulo-based sharding strategy.
 *
 * <p>Database index: {@code shardKey % shardDbCount}<br>
 * Table index: {@code tableKey % shardTableCount}
 */
public class ModuloShardingStrategy implements ShardingStrategy {

  @Override
  public int computeDbIndex(long shardKey, int shardDbCount) {
    if (shardDbCount <= 0) {
      throw new IllegalArgumentException("shardDbCount must be > 0, got: " + shardDbCount);
    }
    return (int) (Math.abs(shardKey) % shardDbCount);
  }

  @Override
  public int computeTableIndex(long tableKey, int shardTableCount) {
    if (shardTableCount <= 0) {
      throw new IllegalArgumentException("shardTableCount must be > 0, got: " + shardTableCount);
    }
    return (int) (Math.abs(tableKey) % shardTableCount);
  }
}
