package cloud.xcan.angus.queue.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mq_dead_letter")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DeadLetterEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String topic;

  @Column(name = "partition_id", nullable = false)
  private Integer partitionId;

  @Column(columnDefinition = "json", nullable = false)
  private String payload;

  @Column(columnDefinition = "json")
  private String headers;

  @Column(nullable = false)
  private Integer attempts;

  @Column(length = 256)
  private String reason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;
}
