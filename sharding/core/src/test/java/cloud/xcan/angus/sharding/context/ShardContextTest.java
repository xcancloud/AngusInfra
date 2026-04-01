package cloud.xcan.angus.sharding.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ShardContextTest {

  @AfterEach
  void cleanup() {
    ShardContext.clear();
  }

  @Test
  void defaultDataSourceKeyIsMasteWhenNoShardSet() {
    assertThat(ShardContext.getDataSourceKey()).isEqualTo(ShardContext.MASTER_DATASOURCE_KEY);
  }

  @Test
  void setAndGetShardInfo() {
    ShardInfo info = new ShardInfo(1L, "shard0DataSource", 2L);
    ShardContext.set(info);
    assertThat(ShardContext.get()).isSameAs(info);
  }

  @Test
  void getDataSourceKeyReturnsShardKey() {
    ShardContext.set(new ShardInfo(100L, "shard1DataSource", -1L));
    assertThat(ShardContext.getDataSourceKey()).isEqualTo("shard1DataSource");
  }

  @Test
  void getDataSourceKeyReturnsMasterWhenDataSourceKeyIsNull() {
    ShardContext.set(new ShardInfo(0L, null, -1L));
    assertThat(ShardContext.getDataSourceKey()).isEqualTo(ShardContext.MASTER_DATASOURCE_KEY);
  }

  @Test
  void clearRemovesShardInfo() {
    ShardContext.set(new ShardInfo(5L, "shard0DataSource", 1L));
    ShardContext.clear();
    assertThat(ShardContext.get()).isNull();
    assertThat(ShardContext.getDataSourceKey()).isEqualTo(ShardContext.MASTER_DATASOURCE_KEY);
  }

  @Test
  void isShardedReturnsFalseByDefault() {
    assertThat(ShardContext.isSharded()).isFalse();
  }

  @Test
  void isShardedReturnsTrueWhenShardSet() {
    ShardContext.set(new ShardInfo(10L, "shard0DataSource", 0L));
    assertThat(ShardContext.isSharded()).isTrue();
  }

  @Test
  void isShardedReturnsFalseForMasterKey() {
    ShardContext.set(new ShardInfo(0L, ShardContext.MASTER_DATASOURCE_KEY, -1L));
    assertThat(ShardContext.isSharded()).isFalse();
  }

  @Test
  void setNullAllowedClearsContext() {
    ShardContext.set(new ShardInfo(1L, "shard0DataSource", 0L));
    ShardContext.set(null);
    assertThat(ShardContext.get()).isNull();
  }
}
