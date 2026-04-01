package cloud.xcan.angus.core.tester.infra.metricsds;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class MetricsDynamicDataSourceRouter extends AbstractRoutingDataSource {

  @Override
  protected Object determineCurrentLookupKey() {
    return MetricsDataSourceContextHolder.getDataSource();
  }
}
