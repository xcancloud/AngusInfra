package cloud.xcan.angus.job.executor;

import cloud.xcan.angus.job.model.JobContext;
import java.util.List;

/**
 * Extended contract for MapReduce-style job executors.
 *
 * <p>The framework calls {@link #map} for each shard in parallel and then
 * collects all results before calling {@link #reduce} on a single node. Implementations must also
 * provide an {@link #execute} method, but the framework never calls it directly — it is provided
 * for API symmetry only and should return {@code null}.
 */
public interface MapReduceJobExecutor extends JobExecutor {

  /**
   * Map phase — processes one data shard.
   *
   * @param context           runtime job context
   * @param shardingItem      zero-based shard index
   * @param shardingParameter parameter string for this specific shard
   * @return a list of intermediate result tokens to pass to the reduce phase
   */
  List<String> map(JobContext context, int shardingItem, String shardingParameter);

  /**
   * Reduce phase — aggregates all map results into a single output.
   *
   * @param context    runtime job context (same as passed to map)
   * @param mapResults concatenated intermediate results from all map shards
   * @return a string representation of the final aggregate result
   */
  String reduce(JobContext context, List<String> mapResults);
}
