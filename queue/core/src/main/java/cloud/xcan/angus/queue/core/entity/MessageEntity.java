package cloud.xcan.angus.queue.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mq_message")
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

  @Column(nullable = false)
  private Integer status;

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
