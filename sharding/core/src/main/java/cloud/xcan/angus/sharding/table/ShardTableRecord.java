package cloud.xcan.angus.sharding.table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Immutable value object representing a shard table that has been physically created.
 *
 * <p>Implementations of {@link ShardTableRegistry} persist and retrieve instances of this class.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "tableName")
@Accessors(chain = true)
public final class ShardTableRecord {

  /**
   * Fully-qualified sharded table name, e.g. {@code exec_sample-100} or {@code exec_sample-100-3}.
   */
  private String tableName;

  /**
   * The shard key that identifies the owning shard (e.g. tenantId or business key).
   */
  private long shardKey;

  /**
   * Zero-based DB shard index computed from {@link #shardKey}.
   */
  private int dbIndex;

  /**
   * Secondary table index; {@code -1} means the table has no secondary index.
   */
  private long tableIndex;

  public ShardTableRecord() {
  }

  public ShardTableRecord(String tableName, long shardKey, int dbIndex, long tableIndex) {
    this.tableName = tableName;
    this.shardKey = shardKey;
    this.dbIndex = dbIndex;
    this.tableIndex = tableIndex;
  }
}
