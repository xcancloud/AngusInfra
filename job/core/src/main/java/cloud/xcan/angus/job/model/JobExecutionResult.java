package cloud.xcan.angus.job.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Result returned by a {@link cloud.xcan.angus.job.executor.JobExecutor} after
 * completing its work.
 */
@Getter
@Builder
public class JobExecutionResult {

  /** {@code true} if the execution completed without errors. */
  private final boolean success;

  /** Human-readable description of the outcome; may be {@code null} on failure. */
  private final String result;

  /** Error description; {@code null} on success. */
  private final String errorMessage;

  /** Wall-clock duration in milliseconds measured by the executor. */
  private final Long executionTime;

  /** Optional structured metrics emitted by the executor (e.g. record counts). */
  private final Map<String, Object> metrics;
}
