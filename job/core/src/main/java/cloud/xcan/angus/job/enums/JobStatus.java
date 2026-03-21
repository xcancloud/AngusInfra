package cloud.xcan.angus.job.enums;

/**
 * Lifecycle status of a scheduled job definition.
 */
public enum JobStatus {
  /** Waiting for next scheduled execution. */
  READY,
  /** Currently executing. */
  RUNNING,
  /** Manually suspended; will not be picked up by the scheduler. */
  PAUSED,
  /** Successfully completed (for one-shot jobs). */
  COMPLETED,
  /** Failed after exhausting all retry attempts. */
  FAILED
}
