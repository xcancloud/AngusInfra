package cloud.xcan.angus.sharding.table;

/**
 * SPI for managing creation and tracking of dynamically sharded tables.
 *
 * <p>Implementations can use SQL DDL templates, Liquibase, Flyway, or any other mechanism
 * to create tenant-specific sharded tables at runtime.
 */
public interface ShardTableManager {

  /**
   * @return true if the given sharded table name has already been created
   */
  boolean isCreated(String shardedTableName);

  /**
   * Ensure all required sharded tables exist for the given shard key.
   *
   * @param shardKey             the primary shard key (e.g. tenant ID)
   * @param shardTableCount      number of table shards
   * @param enableSecondaryIndex whether secondary table indexing is enabled
   */
  void ensureTablesExist(long shardKey, int shardTableCount, boolean enableSecondaryIndex);
}
