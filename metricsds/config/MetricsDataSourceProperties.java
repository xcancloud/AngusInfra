package cloud.xcan.angus.core.tester.infra.metricsds.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Base class for configuration of a data source.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "xcan.datasource.metrics", ignoreUnknownFields = false)
public class MetricsDataSourceProperties {

  /**
   * Value range: 1-10
   */
  private Integer shardDbNum;
  /**
   * Value range: 1-50
   */
  private Integer shardTabledNum;
  private Boolean enableTabledSecondIndex = false;
  private String username;
  private String password;
  private String[] entityPackages;

  private MySql mysql = new MySql();
  private Postgresql postgresql = new Postgresql();

  @Getter
  @Setter
  public static class MySql {

    private String driverClassName;
    private String[] urls;

  }

  @Getter
  @Setter
  public static class Postgresql {

    private String driverClassName;
    private String[] urls;

  }
}
