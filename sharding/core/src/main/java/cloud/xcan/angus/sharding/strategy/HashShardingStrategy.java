package cloud.xcan.angus.sharding.strategy;

/**
 * Consistent hash-based sharding strategy using MurmurHash3-inspired bit-mixing.
 *
 * <p>Compared with simple modulo ({@link ModuloShardingStrategy}), this strategy produces a more
 * uniform key distribution when shard keys are monotonically increasing identifiers (e.g. auto-
 * increment IDs or Snowflake IDs), which tend to cluster under plain modulo.
 *
 * <p>The mixing function is adapted from Austin Appleby's finalizer in MurmurHash3:
 * <pre>
 *   key ^= key >>> 33;
 *   key *= 0xff51afd7ed558ccdL;
 *   key ^= key >>> 33;
 *   key *= 0xc4ceb9fe1a85ec53L;
 *   key ^= key >>> 33;
 * </pre>
 */
public class HashShardingStrategy implements ShardingStrategy {

  @Override
  public int computeDbIndex(long shardKey, int shardDbCount) {
    if (shardDbCount <= 0) {
      throw new IllegalArgumentException("shardDbCount must be > 0, got: " + shardDbCount);
    }
    return (int) (Math.abs(mix(shardKey)) % shardDbCount);
  }

  @Override
  public int computeTableIndex(long tableKey, int shardTableCount) {
    if (shardTableCount <= 0) {
      throw new IllegalArgumentException("shardTableCount must be > 0, got: " + shardTableCount);
    }
    return (int) (Math.abs(mix(tableKey)) % shardTableCount);
  }

  /**
   * MurmurHash3 64-bit finalizer mix.  Returns a well-distributed hash of {@code key}.
   */
  static long mix(long key) {
    key ^= key >>> 33;
    key *= 0xff51afd7ed558ccdL;
    key ^= key >>> 33;
    key *= 0xc4ceb9fe1a85ec53L;
    key ^= key >>> 33;
    return key;
  }
}
