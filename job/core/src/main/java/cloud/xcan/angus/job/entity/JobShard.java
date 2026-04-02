package cloud.xcan.angus.job.entity;

import cloud.xcan.angus.job.enums.ShardStatus;
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
 * One processing shard within a sharded or MapReduce job execution.
 *
 * <p>A new set of shards is created at the start of each job execution, replacing
 * any shards left over from the previous run.  The {@code mapResult} field is only populated for
 * MAP_REDUCE jobs during the map phase.
 */
@Entity
@Table(
    name = "angus_job_shard",
    indexes = {
        @Index(name = "idx_js_job_id", columnList = "job_id"),
        @Index(name = "idx_js_status", columnList = "status")
    }
)
@Getter
@Setter
public class JobShard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "job_id", nullable = false)
  private Long jobId;

  @Column(name = "sharding_item", nullable = false)
  private Integer shardingItem;

  @Column(name = "sharding_parameter", length = 255)
  private String shardingParameter;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private ShardStatus status;

  /**
   * Serialised result of the map phase; populated only for MAP_REDUCE job type.
   */
  @Column(name = "map_result", columnDefinition = "TEXT")
  private String mapResult;

  @Column(name = "executor_node", length = 255)
  private String executorNode;

  @Column(name = "start_time")
  private LocalDateTime startTime;

  @Column(name = "end_time")
  private LocalDateTime endTime;
}
