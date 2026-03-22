package cloud.xcan.angus.job.enums;

/**
 * Type of distributed job.
 */
public enum JobType {
  /**
   * Single-node simple task.
   */
  SIMPLE,
  /**
   * MapReduce task: parallel map phase + single reduce phase.
   */
  MAP_REDUCE,
  /**
   * Data-sharding task: parallel independent shard execution.
   */
  SHARDING
}
