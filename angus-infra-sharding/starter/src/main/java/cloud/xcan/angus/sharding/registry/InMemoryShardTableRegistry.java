package cloud.xcan.angus.sharding.registry;

import cloud.xcan.angus.sharding.table.ShardTableRecord;
import cloud.xcan.angus.sharding.table.ShardTableRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Default in-memory {@link ShardTableRegistry} implementation.
 *
 * <p>All records are kept in a {@link ConcurrentHashMap} and are lost on JVM restart.  This is
 * safe to use when shard-table DDL statements use {@code CREATE TABLE IF NOT EXISTS}, as the tables
 * are idempotently re-created on the next access.
 *
 * <p>This implementation is always registered when no other {@link ShardTableRegistry} bean is
 * present.
 */
public class InMemoryShardTableRegistry implements ShardTableRegistry {

  private final ConcurrentMap<String, ShardTableRecord> store = new ConcurrentHashMap<>();

  @Override
  public List<ShardTableRecord> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<ShardTableRecord> findByShardKey(long shardKey) {
    return store.values().stream()
        .filter(r -> r.getShardKey() == shardKey)
        .collect(Collectors.toList());
  }

  @Override
  public boolean exists(String tableName) {
    return tableName != null && store.containsKey(tableName);
  }

  @Override
  public void save(ShardTableRecord record) {
    if (record != null && record.getTableName() != null) {
      store.put(record.getTableName(), record);
    }
  }

  @Override
  public void saveAll(List<ShardTableRecord> records) {
    if (records == null) {
      return;
    }
    for (ShardTableRecord r : records) {
      save(r);
    }
  }
}
