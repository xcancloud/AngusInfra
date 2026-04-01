package cloud.xcan.angus.sharding.autoconfigure.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.sharding.table.ShardTableRecord;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link JpaShardTableRegistry} using a mock {@link ShardTableJpaRepository}.
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class JpaShardTableRegistryTest {

  @Mock
  private ShardTableJpaRepository repository;

  private JpaShardTableRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new JpaShardTableRegistry(repository);
  }

  @Test
  void findAll_delegatesToRepository() {
    ShardTableEntity entity = entity("exec_sample-100", 100L, 0, -1L);
    given(repository.findAll()).willReturn(List.of(entity));

    List<ShardTableRecord> records = registry.findAll();

    assertThat(records).hasSize(1);
    assertThat(records.get(0).getTableName()).isEqualTo("exec_sample-100");
    assertThat(records.get(0).getShardKey()).isEqualTo(100L);
  }

  @Test
  void findByShardKey_delegatesToRepository() {
    ShardTableEntity e1 = entity("t-5-0", 5L, 1, 0L);
    ShardTableEntity e2 = entity("t-5-1", 5L, 1, 1L);
    given(repository.findByShardKey(5L)).willReturn(List.of(e1, e2));

    List<ShardTableRecord> records = registry.findByShardKey(5L);

    assertThat(records).hasSize(2);
  }

  @Test
  void exists_existsByIdTrue() {
    given(repository.existsById("exec_sample-100")).willReturn(true);
    assertThat(registry.exists("exec_sample-100")).isTrue();
  }

  @Test
  void exists_existsByIdFalse() {
    given(repository.existsById("missing")).willReturn(false);
    assertThat(registry.exists("missing")).isFalse();
  }

  @Test
  void exists_nullReturnsFalse() {
    assertThat(registry.exists(null)).isFalse();
  }

  @Test
  void save_delegatesToRepository() {
    ShardTableRecord record = new ShardTableRecord("t-1", 1L, 0, -1L);
    registry.save(record);
    verify(repository).save(org.mockito.ArgumentMatchers.any(ShardTableEntity.class));
  }

  @Test
  void save_nullIsIgnored() {
    registry.save(null);
    verify(repository, org.mockito.Mockito.never()).save(
        org.mockito.ArgumentMatchers.any());
  }

  @Test
  void save_recordWithNullTableNameIsIgnored() {
    registry.save(new ShardTableRecord(null, 1L, 0, -1L));
    verify(repository, org.mockito.Mockito.never()).save(
        org.mockito.ArgumentMatchers.any());
  }

  @Test
  void saveAll_delegatesInBatch() {
    List<ShardTableRecord> records = List.of(
        new ShardTableRecord("a-1", 1L, 0, -1L),
        new ShardTableRecord("b-1", 1L, 0, 0L)
    );
    registry.saveAll(records);
    verify(repository).saveAll(org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  void saveAll_nullIsIgnored() {
    registry.saveAll(null);
    verify(repository, org.mockito.Mockito.never()).saveAll(
        org.mockito.ArgumentMatchers.any());
  }

  @Test
  void saveAll_emptyListIsIgnored() {
    registry.saveAll(List.of());
    verify(repository, org.mockito.Mockito.never()).saveAll(
        org.mockito.ArgumentMatchers.any());
  }

  // ── Helper ────────────────────────────────────────────────────────────────

  private ShardTableEntity entity(String tableName, long shardKey, int dbIndex, long tableIndex) {
    return new ShardTableEntity()
        .setTableName(tableName)
        .setShardKey(shardKey)
        .setDbIndex(dbIndex)
        .setTableIndex(tableIndex);
  }
}
