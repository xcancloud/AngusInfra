package cloud.xcan.angus.sharding.registry;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.sharding.table.ShardTableRecord;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryShardTableRegistryTest {

  private InMemoryShardTableRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new InMemoryShardTableRegistry();
  }

  @Test
  void initiallyEmpty() {
    assertThat(registry.findAll()).isEmpty();
  }

  @Test
  void saveAndExists() {
    registry.save(new ShardTableRecord("exec_sample-100", 100L, 0, -1L));
    assertThat(registry.exists("exec_sample-100")).isTrue();
  }

  @Test
  void existsReturnsFalseForUnknownTable() {
    assertThat(registry.exists("unknown_table")).isFalse();
  }

  @Test
  void existsReturnsFalseForNull() {
    assertThat(registry.exists(null)).isFalse();
  }

  @Test
  void saveNullIsIgnored() {
    registry.save(null);
    assertThat(registry.findAll()).isEmpty();
  }

  @Test
  void saveRecordWithNullTableNameIsIgnored() {
    registry.save(new ShardTableRecord(null, 0L, 0, -1L));
    assertThat(registry.findAll()).isEmpty();
  }

  @Test
  void findAll_returnsAllSavedRecords() {
    registry.save(new ShardTableRecord("t-1", 1L, 0, -1L));
    registry.save(new ShardTableRecord("t-2", 2L, 0, -1L));
    assertThat(registry.findAll()).hasSize(2);
  }

  @Test
  void findByShardKey_returnsMatchingRecords() {
    registry.save(new ShardTableRecord("t-100-0", 100L, 0, 0L));
    registry.save(new ShardTableRecord("t-100-1", 100L, 0, 1L));
    registry.save(new ShardTableRecord("t-200-0", 200L, 1, 0L));

    List<ShardTableRecord> result = registry.findByShardKey(100L);
    assertThat(result).hasSize(2)
        .extracting(ShardTableRecord::getTableName)
        .containsExactlyInAnyOrder("t-100-0", "t-100-1");
  }

  @Test
  void findByShardKey_emptyWhenNoMatch() {
    registry.save(new ShardTableRecord("t-1", 1L, 0, -1L));
    assertThat(registry.findByShardKey(999L)).isEmpty();
  }

  @Test
  void saveAll_persistsAllRecords() {
    registry.saveAll(List.of(
        new ShardTableRecord("a-1", 1L, 0, -1L),
        new ShardTableRecord("b-1", 1L, 0, -1L)
    ));
    assertThat(registry.findAll()).hasSize(2);
  }

  @Test
  void saveAll_nullIsIgnored() {
    registry.saveAll(null);
    assertThat(registry.findAll()).isEmpty();
  }

  @Test
  void saveAll_emptyListIsIgnored() {
    registry.saveAll(List.of());
    assertThat(registry.findAll()).isEmpty();
  }

  @Test
  void duplicateSaveOverwrites() {
    registry.save(new ShardTableRecord("t-1", 1L, 0, -1L));
    registry.save(new ShardTableRecord("t-1", 2L, 1, 3L));
    assertThat(registry.findAll()).hasSize(1);
    assertThat(registry.findAll().get(0).getShardKey()).isEqualTo(2L);
  }
}
