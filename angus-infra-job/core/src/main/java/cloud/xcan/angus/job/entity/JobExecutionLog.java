package cloud.xcan.angus.job.entity;

import cloud.xcan.angus.job.enums.ExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Immutable audit record of one job execution attempt.
 *
 * <p>A new row is written at execution start and updated on completion or failure.
 * For sharded jobs, one row is written per shard item; for MapReduce jobs, one row is written for
 * the reduce phase and one per map shard.
 */
@Entity
@Table(
    name = "angus_job_execution_log",
    indexes = {
        @Index(name = "idx_jel_job_id", columnList = "job_id"),
        @Index(name = "idx_jel_start_time", columnList = "start_time"),
        @Index(name = "idx_jel_status", columnList = "status")
    }
)
@Getter
@Setter
public class JobExecutionLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "job_id", nullable = false)
  private Long jobId;

  @Column(name = "job_name", nullable = false, length = 255)
  private String jobName;

  /**
   * Shard index for sharded/map executions; {@code null} for the reduce phase sentinel value.
   */
  @Column(name = "sharding_item")
  private Integer shardingItem;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private ExecutionStatus status;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time")
  private LocalDateTime endTime;

  /**
   * Wall-clock duration of the execution in milliseconds.
   */
  @Column(name = "execution_time")
  private Long executionTime;

  @Column(name = "result", columnDefinition = "TEXT")
  private String result;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  /**
   * Identifier of the cluster node that executed this record.
   */
  @Column(name = "executor_node", length = 255)
  private String executorNode;
}
