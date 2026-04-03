package cloud.xcan.angus.sharding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import cloud.xcan.angus.sharding.table.ShardTableRegistry;
import java.util.List;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SqlTemplateTableManager} using H2 as the backing database.
 */
class SqlTemplateTableManagerTest {

  private DataSource dataSource;

  @BeforeEach
  void setUp() {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:shard_mgr_test_" + System.nanoTime()
        + ";DB_CLOSE_DELAY=-1;MODE=MySQL");
    ds.setUser("sa");
    dataSource = ds;
  }

  @Test
  void isCreated_falseBeforeCreation() {
    SqlTemplateTableManager mgr = new SqlTemplateTableManager(dataSource, "sharding/schema",
        List.of("test_shard_table"));
    assertThat(mgr.isCreated("test_shard_table-100")).isFalse();
  }

  @Test
  void ensureTablesExist_createsTablesFromTemplate() {
    // Our test DDL is at src/test/resources/sharding/schema/test_shard_table.sql
    SqlTemplateTableManager mgr = new SqlTemplateTableManager(dataSource, "sharding/schema",
        List.of("test_shard_table"));
    mgr.ensureTablesExist(100L, 2, false);

    // Without secondary index, tables are: test_shard_table-100
    assertThat(mgr.isCreated("test_shard_table-100")).isTrue();
  }

  @Test
  void ensureTablesExist_withSecondaryIndex() {
    SqlTemplateTableManager mgr = new SqlTemplateTableManager(dataSource, "sharding/schema",
        List.of("test_shard_table"));
    mgr.ensureTablesExist(200L, 3, true);

    for (int i = 0; i < 3; i++) {
      assertThat(mgr.isCreated("test_shard_table-200-" + i)).isTrue();
    }
  }

  @Test
  void ensureTablesExist_idempotent() {
    SqlTemplateTableManager mgr = new SqlTemplateTableManager(dataSource, "sharding/schema",
        List.of("test_shard_table"));
    mgr.ensureTablesExist(300L, 1, false);
    // Second call should not throw
    mgr.ensureTablesExist(300L, 1, false);
    assertThat(mgr.isCreated("test_shard_table-300")).isTrue();
  }

  @Test
  void ensureTablesExist_noTemplates_doesNothing() {
    SqlTemplateTableManager mgr = new SqlTemplateTableManager(dataSource, "sharding/schema", null);
    // should not throw
    mgr.ensureTablesExist(1L, 1, false);
    assertThat(mgr.isCreated("test_shard_table-1")).isFalse();
  }

  @Test
  void ensureTablesExist_withRegistry_persistsRecords() {
    ShardTableRegistry registry = mock(ShardTableRegistry.class);
    given(registry.findAll()).willReturn(List.of());

    SqlTemplateTableManager mgr = new SqlTemplateTableManager(dataSource, "sharding/schema",
        List.of("test_shard_table"), registry);
    mgr.ensureTablesExist(400L, 1, false);

    org.mockito.Mockito.verify(registry).saveAll(org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  void loadTemplate_returnsNullForMissingFile() {
    SqlTemplateTableManager mgr = new SqlTemplateTableManager(dataSource, "sharding/schema",
        List.of());
    assertThat(mgr.loadTemplate("nonexistent_table")).isNull();
  }

  @Test
  void removeSecondaryIndexes_removesCreateIndexStatements() {
    String ddl = "CREATE TABLE t (id BIGINT);\nCREATE INDEX idx_t_id ON t (id);\n";
    String result = SqlTemplateTableManager.removeSecondaryIndexes(ddl);
    assertThat(result).doesNotContainIgnoringCase("CREATE INDEX");
    assertThat(result).contains("CREATE TABLE");
  }

  @Test
  void constructorWithRegistry_prewarmsFromRegistry() {
    cloud.xcan.angus.sharding.table.ShardTableRecord prewarmed =
        new cloud.xcan.angus.sharding.table.ShardTableRecord("test_shard_table-999", 999L, 0, -1L);
    ShardTableRegistry registry = mock(ShardTableRegistry.class);
    given(registry.findAll()).willReturn(List.of(prewarmed));

    SqlTemplateTableManager mgr = new SqlTemplateTableManager(dataSource, "sharding/schema",
        List.of("test_shard_table"), registry);

    assertThat(mgr.isCreated("test_shard_table-999")).isTrue();
  }
}
