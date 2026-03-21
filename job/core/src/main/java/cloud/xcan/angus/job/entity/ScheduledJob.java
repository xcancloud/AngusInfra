package cloud.xcan.angus.job.entity;

import cloud.xcan.angus.job.enums.JobStatus;
import cloud.xcan.angus.job.enums.JobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Persistent definition of a scheduled job.
 *
 * <p>One row represents one recurring or one-shot job registered in the system.
 * The scheduler polls rows in READY state whose {@code nextExecuteTime} is in
 * the past and dispatches them for execution under a distributed lock.
 */
@Entity
@Table(
    name = "scheduled_job",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_job_name_group",
        columnNames = {"job_name", "job_group"}
    ),
    indexes = @Index(
        name = "idx_sj_status_next_exec",
        columnList = "status, next_execute_time"
    )
)
@Getter
@Setter
public class ScheduledJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "job_name", nullable = false, length = 255)
  private String jobName;

  @Column(name = "job_group", nullable = false, length = 255)
  private String jobGroup;

  @Column(name = "cron_expression", nullable = false, length = 255)
  private String cronExpression;

  /**
   * Spring bean name that implements {@link cloud.xcan.angus.job.executor.JobExecutor}.
   * Must be registered in the application context before the job is triggered.
   */
  @Column(name = "bean_name", nullable = false, length = 255)
  private String beanName;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false, length = 50)
  private JobType jobType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private JobStatus status;

  /** Number of data shards; relevant for MAP_REDUCE and SHARDING job types. */
  @Column(name = "sharding_count")
  private Integer shardingCount;

  /**
   * Comma-separated shard parameters, one per shard.
   * Length must match {@code shardingCount} when provided.
   */
  @Column(name = "sharding_parameter", columnDefinition = "TEXT")
  private String shardingParameter;

  @Column(name = "retry_count")
  private Integer retryCount;

  @Column(name = "max_retry_count")
  private Integer maxRetryCount;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "last_execute_time")
  private LocalDateTime lastExecuteTime;

  @Column(name = "next_execute_time")
  private LocalDateTime nextExecuteTime;

  @Column(name = "create_time", nullable = false)
  private LocalDateTime createTime;

  @Column(name = "update_time", nullable = false)
  private LocalDateTime updateTime;
}
