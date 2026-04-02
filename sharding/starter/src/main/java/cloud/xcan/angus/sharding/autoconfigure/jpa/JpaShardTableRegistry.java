package cloud.xcan.angus.sharding.autoconfigure.jpa;

import cloud.xcan.angus.sharding.table.ShardTableRecord;
import cloud.xcan.angus.sharding.table.ShardTableRegistry;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA-backed {@link ShardTableRegistry} that delegates to {@link ShardTableJpaRepository}.
 *
 * <p>This implementation is auto-registered by
 * {@link ShardingJpaAutoConfiguration} whenever a {@link ShardTableJpaRepository} bean is present
 * on the application context.  It provides durable, cross-restart shard table tracking through the
 * primary datasource.
 */
@Slf4j
@RequiredArgsConstructor
public class JpaShardTableRegistry implements ShardTableRegistry {

  private final ShardTableJpaRepository repository;

  @Override
  public List<ShardTableRecord> findAll() {
    return repository.findAll().stream()
        .map(this::toRecord)
        .collect(Collectors.toList());
  }

  @Override
  public List<ShardTableRecord> findByShardKey(long shardKey) {
    return repository.findByShardKey(shardKey).stream()
        .map(this::toRecord)
        .collect(Collectors.toList());
  }

  @Override
  public boolean exists(String tableName) {
    if (tableName == null) {
      return false;
    }
    return repository.existsById(tableName);
  }

  @Override
  public void save(ShardTableRecord record) {
    if (record == null || record.getTableName() == null) {
      return;
    }
    repository.save(toEntity(record));
  }

  @Override
  public void saveAll(List<ShardTableRecord> records) {
    if (records == null || records.isEmpty()) {
      return;
    }
    repository.saveAll(records.stream().map(this::toEntity).collect(Collectors.toList()));
  }

  // ── Mapping helpers ─────────────────────────────────────────────────────────

  private ShardTableRecord toRecord(ShardTableEntity entity) {
    return new ShardTableRecord(
        entity.getTableName(),
        entity.getShardKey(),
        entity.getDbIndex(),
        entity.getTableIndex()
    );
  }

  private ShardTableEntity toEntity(ShardTableRecord record) {
    return new ShardTableEntity()
        .setTableName(record.getTableName())
        .setShardKey(record.getShardKey())
        .setDbIndex(record.getDbIndex())
        .setTableIndex(record.getTableIndex());
  }
}
