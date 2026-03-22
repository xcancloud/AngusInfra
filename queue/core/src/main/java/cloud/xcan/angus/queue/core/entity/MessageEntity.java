package cloud.xcan.angus.queue.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mq_message", indexes = {
    // leaseBatch: WHERE topic=? AND partition_id IN (?) AND status=0 AND visible_at<=NOW()
    @Index(name = "idx_mq_msg_topic_status_visible", columnList = "topic, status, visible_at"),
    // reclaimExpiredLeases: WHERE status=1 AND lease_until<NOW()
    @Index(name = "idx_mq_msg_status_lease_until", columnList = "status, lease_until"),
    // findLeasedByOwner: WHERE lease_owner=? AND status=1 AND lease_until>=NOW()
    @Index(name = "idx_mq_msg_lease_owner", columnList = "lease_owner"),
    // findExceededAttempts: WHERE attempts >= max_attempts
    @Index(name = "idx_mq_msg_attempts", columnList = "attempts")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MessageEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String topic;

  @Column(name = "partition_id", nullable = false)
  private Integer partitionId;

  @Column(nullable = false)
  private Integer priority = 0;

  @Column(columnDefinition = "json", nullable = false)
  private String payload;

  @Column(columnDefinition = "json")
  private String headers;

  @Enumerated(EnumType.ORDINAL)
  @Column(nullable = false)
  private MessageStatus status;

  @Column(name = "visible_at", nullable = false)
  private Instant visibleAt;

  @Column(name = "lease_until")
  private Instant leaseUntil;

  @Column(name = "lease_owner", length = 128)
  private String leaseOwner;

  @Column(nullable = false)
  private Integer attempts = 0;

  @Column(name = "max_attempts", nullable = false)
  private Integer maxAttempts = 16;

  @Column(name = "idempotency_key", length = 256)
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  private Long version;
}
