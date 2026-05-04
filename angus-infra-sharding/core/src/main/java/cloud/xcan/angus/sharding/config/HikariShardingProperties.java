package cloud.xcan.angus.sharding.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HikariCP pool configuration for shard data sources.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "angus.sharding.hikari")
public class HikariShardingProperties {

  private int minimumIdle = -1;

  private int maximumPoolSize = -1;

  private long maxLifetime = MINUTES.toMillis(30);

  private long connectionTimeout = SECONDS.toMillis(30);

  private long validationTimeout = SECONDS.toMillis(5);

  private long idleTimeout = MINUTES.toMillis(10);

  private int initializationFailTimeout = 1;

  private boolean autoCommit = true;

  private boolean readOnly = false;

  private String connectionTestQuery = "SELECT 1";

  private String poolName = "angusShardingCP";

  private long leakDetectionThreshold = SECONDS.toMillis(30);
}
