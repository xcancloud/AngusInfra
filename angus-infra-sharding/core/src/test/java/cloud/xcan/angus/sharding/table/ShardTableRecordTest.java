package cloud.xcan.angus.sharding.table;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShardTableRecordTest {

  @Test
  void noArgConstructorAndSetters() {
    ShardTableRecord r = new ShardTableRecord();
    r.setTableName("exec_sample-100").setShardKey(100L).setDbIndex(0).setTableIndex(2L);
    assertThat(r.getTableName()).isEqualTo("exec_sample-100");
    assertThat(r.getShardKey()).isEqualTo(100L);
    assertThat(r.getDbIndex()).isEqualTo(0);
    assertThat(r.getTableIndex()).isEqualTo(2L);
  }

  @Test
  void allArgsConstructor() {
    ShardTableRecord r = new ShardTableRecord("node_usage-5-3", 5L, 1, 3L);
    assertThat(r.getTableName()).isEqualTo("node_usage-5-3");
    assertThat(r.getShardKey()).isEqualTo(5L);
    assertThat(r.getDbIndex()).isEqualTo(1);
    assertThat(r.getTableIndex()).isEqualTo(3L);
  }

  @Test
  void equalityBasedOnTableName() {
    ShardTableRecord a = new ShardTableRecord("exec_sample-1", 1L, 0, -1L);
    ShardTableRecord b = new ShardTableRecord("exec_sample-1", 999L, 9, 99L);
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void inequalityOnDifferentTableName() {
    ShardTableRecord a = new ShardTableRecord("exec_sample-1", 1L, 0, -1L);
    ShardTableRecord b = new ShardTableRecord("exec_sample-2", 1L, 0, -1L);
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void toStringContainsTableName() {
    ShardTableRecord r = new ShardTableRecord("test_table-42-1", 42L, 0, 1L);
    assertThat(r.toString()).contains("test_table-42-1");
  }

  @Test
  void chaining() {
    ShardTableRecord r = new ShardTableRecord()
        .setTableName("t-1")
        .setShardKey(1L)
        .setDbIndex(0)
        .setTableIndex(-1L);
    assertThat(r.getTableName()).isEqualTo("t-1");
  }
}
