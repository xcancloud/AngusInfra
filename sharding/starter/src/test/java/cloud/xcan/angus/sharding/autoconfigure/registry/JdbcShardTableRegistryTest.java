package cloud.xcan.angus.sharding.autoconfigure.registry;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.sharding.table.ShardTableRecord;
import java.util.List;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration-style test for {@link JdbcShardTableRegistry} using an H2 in-memory datasource.
 */
class JdbcShardTableRegistryTest {

  private JdbcShardTableRegistry registry;

  @BeforeEach
  void setUp() {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:shard_registry_test_" + System.nanoTime()
        + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
    ds.setUser("sa");
    registry = new JdbcShardTableRegistry(ds, "angus_shard_table");
  }

  @Test
  void schemaCreatedAutomatically() {
    // If schema is not created, findAll() would throw; verifying no exception
    assertThat(registry.findAll()).isEmpty();
  }

  @Test
  void saveAndExists() {
    registry.save(new ShardTableRecord("exec_sample-100", 100L, 0, -1L));
    assertThat(registry.exists("exec_sample-100")).isTrue();
  }

  @Test
  void existsReturnsFalseForMissingTable() {
    assertThat(registry.exists("not_there")).isFalse();
  }

  @Test
  void existsForNull() {
    assertThat(registry.exists(null)).isFalse();
  }

  @Test
  void findAll_returnsAllSaved() {
    registry.save(new ShardTableRecord("t-1", 1L, 0, -1L));
    registry.save(new ShardTableRecord("t-2", 2L, 1, -1L));
    assertThat(registry.findAll()).hasSize(2);
  }

  @Test
  void findByShardKey_filtersCorrectly() {
    registry.save(new ShardTableRecord("a-10", 10L, 0, -1L));
    registry.save(new ShardTableRecord("b-10", 10L, 0, 1L));
    registry.save(new ShardTableRecord("c-20", 20L, 1, -1L));
    List<ShardTableRecord> result = registry.findByShardKey(10L);
    assertThat(result).hasSize(2)
        .extracting(ShardTableRecord::getTableName)
        .containsExactlyInAnyOrder("a-10", "b-10");
  }

  @Test
  void saveAll_persistsMultipleRecords() {
    registry.saveAll(List.of(
        new ShardTableRecord("x-1", 1L, 0, -1L),
        new ShardTableRecord("y-1", 1L, 0, 0L)
    ));
    assertThat(registry.findAll()).hasSize(2);
  }

  @Test
  void saveNull_doesNotThrow() {
    registry.save(null);
    assertThat(registry.findAll()).isEmpty();
  }

  @Test
  void saveNullList_doesNotThrow() {
    registry.saveAll(null);
    assertThat(registry.findAll()).isEmpty();
  }

  @Test
  void duplicateInsert_doesNotFail() {
    ShardTableRecord r = new ShardTableRecord("dup-1", 1L, 0, -1L);
    registry.save(r);
    // Second save should be silently ignored (ON CONFLICT DO NOTHING)
    registry.save(new ShardTableRecord("dup-1", 99L, 9, 99L));
    assertThat(registry.findAll()).hasSize(1);
    // Original data preserved
    assertThat(registry.findAll().get(0).getShardKey()).isEqualTo(1L);
  }
}
