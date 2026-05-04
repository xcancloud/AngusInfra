package cloud.xcan.angus.sharding.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShardInfoTest {

  @Test
  void constructorAndGetters() {
    ShardInfo info = new ShardInfo(42L, "shard1DataSource", 3L);
    assertThat(info.getShardKey()).isEqualTo(42L);
    assertThat(info.getDataSourceKey()).isEqualTo("shard1DataSource");
    assertThat(info.getTableIndex()).isEqualTo(3L);
  }

  @Test
  void hasTableIndexReturnsTrueWhenPositive() {
    ShardInfo info = new ShardInfo(1L, "ds", 0L);
    assertThat(info.hasTableIndex()).isTrue();
  }

  @Test
  void hasTableIndexReturnsTrueWhenZero() {
    ShardInfo info = new ShardInfo(1L, "ds", 0L);
    assertThat(info.hasTableIndex()).isTrue();
  }

  @Test
  void hasTableIndexReturnsFalseWhenNegative() {
    ShardInfo info = new ShardInfo(1L, "ds", -1L);
    assertThat(info.hasTableIndex()).isFalse();
  }

  @Test
  void equalityByAllFields() {
    ShardInfo a = new ShardInfo(10L, "shard0DataSource", 2L);
    ShardInfo b = new ShardInfo(10L, "shard0DataSource", 2L);
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void inequalityOnDifferentShardKey() {
    ShardInfo a = new ShardInfo(1L, "ds", 0L);
    ShardInfo b = new ShardInfo(2L, "ds", 0L);
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void toStringContainsAllFields() {
    ShardInfo info = new ShardInfo(99L, "shard2DataSource", 4L);
    String s = info.toString();
    assertThat(s).contains("99").contains("shard2DataSource").contains("4");
  }
}
