package cloud.xcan.angus.core.tester.infra.metricsds.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "xcan.datasource.extra", ignoreUnknownFields = true)
public class MetricsDataSourceExtraProperties {

  /**
   * Supporting databases: MYSQL、POSTGRES
   */
  private String dbType;

  /**
   * Supporting node: single、master-slave
   */
  private String dbMode;

}
