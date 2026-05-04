package cloud.xcan.angus.sharding.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable descriptor carrying the resolved shard information for the current operation.
 */
@Getter
@ToString
@EqualsAndHashCode
public final class ShardInfo {

  private final long shardKey;
  private final String dataSourceKey;
  private final long tableIndex;

  public ShardInfo(long shardKey, String dataSourceKey, long tableIndex) {
    this.shardKey = shardKey;
    this.dataSourceKey = dataSourceKey;
    this.tableIndex = tableIndex;
  }

  /**
   * @return true if table-level secondary indexing is active (tableIndex >= 0)
   */
  public boolean hasTableIndex() {
    return tableIndex >= 0;
  }
}
