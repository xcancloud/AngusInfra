package cloud.xcan.angus.job.enums;

/**
 * Execution status of an individual job shard.
 */
public enum ShardStatus {
  /** Shard has been created but not yet dispatched. */
  PENDING,
  /** Shard is currently being processed. */
  RUNNING,
  /** Shard completed successfully. */
  COMPLETED,
  /** Shard failed. */
  FAILED
}
