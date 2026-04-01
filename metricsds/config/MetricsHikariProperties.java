package cloud.xcan.angus.core.tester.infra.metricsds.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.zaxxer.hikari.HikariConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @see HikariConfig
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "xcan.datasource.hikari", ignoreUnknownFields = false)
public class MetricsHikariProperties {

  private int minimumIdle = -1;

  private int maximumPoolSize = -1;

  private long maxLifetime = MINUTES.toMillis(30);

  private long connectionTimeout = SECONDS.toMillis(30);

  private long validationTimeout = SECONDS.toMillis(5);

  private long idleTimeout = MINUTES.toMillis(10);

  private int initializationFailTimeout = 1;

  private boolean isAutoCommit = true;

  private boolean readOnly = false;

  private String connectionTestQuery = "SELECT 1 FROM DUAL";

  private String poolName = "xcanHikariCP";

}
