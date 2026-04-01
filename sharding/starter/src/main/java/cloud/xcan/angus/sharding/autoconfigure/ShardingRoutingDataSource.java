package cloud.xcan.angus.sharding.autoconfigure;

import cloud.xcan.angus.sharding.context.ShardContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Dynamic data source router that selects the target data source based on the current
 * {@link ShardContext} thread-local.
 */
@Slf4j
public class ShardingRoutingDataSource extends AbstractRoutingDataSource {

  @Override
  protected Object determineCurrentLookupKey() {
    return ShardContext.getDataSourceKey();
  }
}
