package cloud.xcan.angus.queue.core.model;

import java.time.Instant;
import lombok.Data;

@Data
public class MessageData {

  private Long id;
  private String topic;
  private Integer partitionId;
  private Integer priority;
  private String payload;
  private String headers;
  private Integer status;
  private Instant visibleAt;
  private Instant leaseUntil;
  private String leaseOwner;
  private Integer attempts;
  private Integer maxAttempts;
  private String idempotencyKey;
  private Instant createdAt;
  private Instant updatedAt;
}
