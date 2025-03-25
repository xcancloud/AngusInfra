package cloud.xcan.angus.datasource.config;

import cloud.xcan.angus.api.enums.SupportedDbType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "xcan.datasource.extra")
public class DataSourceExtraProperties {

  /**
   * Supporting databases: mysql、postgres
   */
  private SupportedDbType dbType;

  /**
   * Supporting mode: single、master-slave
   */
  private String dbMode;

  private String[] entityPackages;

}
