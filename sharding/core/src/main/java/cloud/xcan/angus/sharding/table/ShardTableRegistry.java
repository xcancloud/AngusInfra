package cloud.xcan.angus.sharding.table;

import java.util.List;

/**
 * SPI for persisting and querying shard table registration records.
 *
 * <p>The framework ships three built-in implementations:
 * <ul>
 *   <li>{@code InMemoryShardTableRegistry} – ephemeral in-process cache (default). Suitable for
 *       single-node deployments or when table creation is idempotent (e.g. {@code CREATE TABLE IF
 *       NOT EXISTS}).</li>
 *   <li>{@code JdbcShardTableRegistry} – durable, backed by a {@code shard_table} row in the
 *       primary datasource. Enabled by setting {@code angus.sharding.table-registry-enabled=true}.
 *       </li>
 *   <li>{@code JpaShardTableRegistry} – durable, backed by a JPA entity repository in the primary
 *       datasource. Activated automatically when a {@code ShardTableJpaRepository} bean is present
 *       on the application context.</li>
 * </ul>
 *
 * <p>Applications may replace any built-in implementation by registering their own
 * {@code ShardTableRegistry} bean.
 */
public interface ShardTableRegistry {

  /**
   * Returns all registered shard table records.
   */
  List<ShardTableRecord> findAll();

  /**
   * Returns all records whose {@link ShardTableRecord#getShardKey()} matches the given value.
   */
  List<ShardTableRecord> findByShardKey(long shardKey);

  /**
   * Returns {@code true} if a record with the given table name exists.
   */
  boolean exists(String tableName);

  /**
   * Registers a single record.
   */
  void save(ShardTableRecord record);

  /**
   * Batch-registers multiple records.
   */
  void saveAll(List<ShardTableRecord> records);
}
