package cloud.xcan.angus.job.model;

import cloud.xcan.angus.job.enums.JobType;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Immutable runtime context supplied to a {@link cloud.xcan.angus.job.executor.JobExecutor}
 * when it is invoked by the scheduler.
 */
@Getter
@Builder
public class JobContext {

  private final Long jobId;
  private final String jobName;
  private final String jobGroup;
  private final JobType jobType;

  /** Zero-based shard index; {@code null} for SIMPLE jobs and the reduce phase. */
  private final Integer shardingItem;

  /** The parameter string bound to this shard; {@code null} for SIMPLE jobs. */
  private final String shardingParameter;

  /** Total number of shards for this execution; {@code null} for SIMPLE jobs. */
  private final Integer totalShardingCount;

  /** Arbitrary key→value parameters attached to the job definition. */
  private final Map<String, Object> parameters;

  /** Wall-clock time at which the scheduler dispatched this execution. */
  private final LocalDateTime executeTime;
}
