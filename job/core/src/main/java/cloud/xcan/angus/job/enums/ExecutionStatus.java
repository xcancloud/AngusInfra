package cloud.xcan.angus.job.enums;

/**
 * Status of a single job execution record.
 */
public enum ExecutionStatus {
  /** Execution is currently in progress. */
  RUNNING,
  /** Execution completed successfully. */
  SUCCESS,
  /** Execution failed with an error. */
  FAILURE,
  /** Execution exceeded the allowed time limit. */
  TIMEOUT
}
