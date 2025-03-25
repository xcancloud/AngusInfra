package cloud.xcan.angus.datasource.config;

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
@ConfigurationProperties(prefix = "xcan.datasource.hikari")
public class HikariProperties {

  private int minimumIdle = 8;
  private int maximumPoolSize = 32;
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
