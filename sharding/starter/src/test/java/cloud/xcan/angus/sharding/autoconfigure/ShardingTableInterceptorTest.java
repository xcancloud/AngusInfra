package cloud.xcan.angus.sharding.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cloud.xcan.angus.sharding.annotation.ShardedTable;
import cloud.xcan.angus.sharding.config.ShardingProperties;
import cloud.xcan.angus.sharding.context.ShardContext;
import cloud.xcan.angus.sharding.context.ShardInfo;
import cloud.xcan.angus.sharding.table.ShardTableManager;
import jakarta.persistence.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShardingTableInterceptorTest {

  private ShardingProperties properties;
  private ShardingTableInterceptor interceptor;

  @BeforeEach
  void setUp() {
    properties = new ShardingProperties();
    properties.setShardTableCount(5);
    interceptor = new ShardingTableInterceptor(properties);
  }

  @AfterEach
  void cleanup() {
    ShardContext.clear();
  }

  // ── inspect – no shard active ─────────────────────────────────────────────

  @Test
  void inspect_returnsOriginalSqlWhenNotSharded() {
    String sql = "select * from exec_sample where id = 1";
    assertThat(interceptor.inspect(sql)).isEqualTo(sql);
  }

  // ── inspect – shard active, table not registered ─────────────────────────

  @Test
  void inspect_returnsOriginalSqlWhenTableNotRegistered() {
    ShardContext.set(new ShardInfo(100L, "shard0DataSource", -1L));
    String sql = "select * from other_table where id = 1";
    assertThat(interceptor.inspect(sql)).isEqualTo(sql);
  }

  // ── inspect – shard active, table registered ──────────────────────────────

  @Test
  void inspect_rewritesTableNameForRegisteredTable() {
    interceptor.registerTable("exec_sample");
    ShardContext.set(new ShardInfo(100L, "shard0DataSource", -1L));

    String sql = "select * from `exec_sample` where id = 1";
    String result = interceptor.inspect(sql);

    assertThat(result).contains("exec_sample-100");
    assertThat(result).doesNotContain("`exec_sample`");
  }

  @Test
  void inspect_rewritesTableNameWithSecondaryIndex() {
    interceptor.registerTable("exec_sample");
    ShardContext.set(new ShardInfo(100L, "shard0DataSource", 3L));

    String sql = "select * from `exec_sample` where id = 1";
    String result = interceptor.inspect(sql);

    assertThat(result).contains("exec_sample-100-3");
  }

  @Test
  void inspect_callsEnsureTablesExistWhenTableManagerPresentAndNotCreated() {
    interceptor.registerTable("exec_sample");
    ShardContext.set(new ShardInfo(100L, "shard0DataSource", -1L));

    ShardTableManager manager = mock(ShardTableManager.class);
    given(manager.isCreated("exec_sample-100")).willReturn(false);
    interceptor.setTableManager(manager);

    interceptor.inspect("select * from `exec_sample`");

    verify(manager).ensureTablesExist(100L, 5, false);
  }

  @Test
  void inspect_skipsEnsureWhenTableAlreadyCreated() {
    interceptor.registerTable("exec_sample");
    ShardContext.set(new ShardInfo(100L, "shard0DataSource", -1L));

    ShardTableManager manager = mock(ShardTableManager.class);
    given(manager.isCreated("exec_sample-100")).willReturn(true);
    interceptor.setTableManager(manager);

    interceptor.inspect("select * from `exec_sample`");

    verify(manager, org.mockito.Mockito.never()).ensureTablesExist(
        org.mockito.ArgumentMatchers.anyLong(),
        org.mockito.ArgumentMatchers.anyInt(),
        org.mockito.ArgumentMatchers.anyBoolean());
  }

  // ── scanAndRegister ──────────────────────────────────────────────────────

  @Test
  void scanAndRegister_detectsAnnotatedEntities() {
    interceptor.scanAndRegister(getClass().getPackageName());
    // The test entity SampleShardedEntity is in this package and has @ShardedTable
    assertThat(interceptor.getShardedTables()).contains("sample_sharded");
  }

  // ── registerTable + getShardedTables ─────────────────────────────────────

  @Test
  void registerTable_addsToSet() {
    interceptor.registerTable("my_table");
    assertThat(interceptor.getShardedTables()).contains("my_table");
  }

  // ── buildShardedTableName ────────────────────────────────────────────────

  @Test
  void buildShardedTableName_withoutSecondaryIndex() {
    ShardContext.set(new ShardInfo(42L, "shard0DataSource", -1L));
    assertThat(interceptor.buildShardedTableName("exec_sample")).isEqualTo("exec_sample-42");
  }

  @Test
  void buildShardedTableName_withSecondaryIndex() {
    ShardContext.set(new ShardInfo(42L, "shard0DataSource", 2L));
    assertThat(interceptor.buildShardedTableName("exec_sample")).isEqualTo("exec_sample-42-2");
  }

  @Test
  void buildShardedTableName_returnsOriginalWhenNoShardContext() {
    assertThat(interceptor.buildShardedTableName("exec_sample")).isEqualTo("exec_sample");
  }

  // ── toSnakeCase ──────────────────────────────────────────────────────────

  @Test
  void toSnakeCase_convertsCorrectly() {
    assertThat(ShardingTableInterceptor.toSnakeCase("ExecSample")).isEqualTo("exec_sample");
    assertThat(ShardingTableInterceptor.toSnakeCase("JvmServiceUsage"))
        .isEqualTo("jvm_service_usage");
    assertThat(ShardingTableInterceptor.toSnakeCase("lowercase")).isEqualTo("lowercase");
  }

  // ── Inner test entity (in this package so scanAndRegister finds it) ──────

  @ShardedTable
  @Table(name = "sample_sharded")
  static class SampleShardedEntity {

  }
}
