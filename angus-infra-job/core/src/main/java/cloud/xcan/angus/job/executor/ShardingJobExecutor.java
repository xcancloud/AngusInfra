package cloud.xcan.angus.job.executor;

import cloud.xcan.angus.job.model.JobContext;
import cloud.xcan.angus.job.model.JobExecutionResult;

/**
 * Extended contract for data-sharding job executors.
 *
 * <p>The framework dispatches each shard to a separate parallel task.
 * Implementations must also provide an {@link #execute} method, but the framework never calls it
 * directly — it is provided for API symmetry only and should return {@code null}.
 */
public interface ShardingJobExecutor extends JobExecutor {

  /**
   * Executes a single data shard.
   *
   * @param context           runtime job context
   * @param shardingItem      zero-based shard index
   * @param shardingParameter parameter string for this specific shard
   * @return a non-null result describing success or failure for this shard
   */
  JobExecutionResult executeSharding(JobContext context, int shardingItem,
      String shardingParameter);
}
