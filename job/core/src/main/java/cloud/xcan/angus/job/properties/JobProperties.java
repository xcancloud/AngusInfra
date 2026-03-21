package cloud.xcan.angus.job.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Job scheduler module.
 *
 * <p>All properties are prefixed with {@code angus.job} in {@code application.yml}.
 *
 * <pre>{@code
 * angus:
 *   job:
 *     scan-interval-ms: 1000
 *     lock-timeout-seconds: 300
 *     executor-core-pool-size: 10
 *     executor-max-pool-size: 50
 *     executor-queue-capacity: 1000
 *     scheduler-pool-size: 20
 *     retry-backoff-minutes: 5
 * }</pre>
 */
@ConfigurationProperties(prefix = "angus.job")
@Getter
@Setter
public class JobProperties {

  /**
   * Interval in milliseconds between successive scans for due jobs.
   * Lower values reduce scheduling latency but increase DB read load.
   */
  private long scanIntervalMs = 1_000;

  /**
   * Maximum number of seconds that a distributed lock is held.
   * Should be set comfortably above the expected worst-case job duration.
   */
  private int lockTimeoutSeconds = 300;

  /** Core thread count for the shared job-executor pool. */
  private int executorCorePoolSize = 10;

  /** Maximum thread count for the shared job-executor pool. */
  private int executorMaxPoolSize = 50;

  /** Work-queue capacity for the shared job-executor pool. */
  private int executorQueueCapacity = 1_000;

  /** Thread count for the Spring task-scheduler pool (used for @Scheduled tasks). */
  private int schedulerPoolSize = 5;

  /** Minutes to wait before retrying a failed job. */
  private int retryBackoffMinutes = 5;

  /** Maximum minutes a job may run before being considered timed-out by the monitor. */
  private int timeoutThresholdMinutes = 30;
}
