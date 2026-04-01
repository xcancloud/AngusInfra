package cloud.xcan.angus.sharding.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.sharding.context.ShardContext;
import cloud.xcan.angus.sharding.context.ShardInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ShardingRoutingDataSourceTest {

  private final ShardingRoutingDataSource routingDataSource = new ShardingRoutingDataSource();

  @AfterEach
  void cleanup() {
    ShardContext.clear();
  }

  @Test
  void determineCurrentLookupKey_returnsMasterWhenNoShardSet() {
    assertThat(routingDataSource.determineCurrentLookupKey())
        .isEqualTo(ShardContext.MASTER_DATASOURCE_KEY);
  }

  @Test
  void determineCurrentLookupKey_returnsShardKeyWhenSet() {
    ShardContext.set(new ShardInfo(5L, "shard2DataSource", -1L));
    assertThat(routingDataSource.determineCurrentLookupKey()).isEqualTo("shard2DataSource");
  }

  @Test
  void determineCurrentLookupKey_returnsMasterAfterClear() {
    ShardContext.set(new ShardInfo(5L, "shard1DataSource", -1L));
    ShardContext.clear();
    assertThat(routingDataSource.determineCurrentLookupKey())
        .isEqualTo(ShardContext.MASTER_DATASOURCE_KEY);
  }
}
