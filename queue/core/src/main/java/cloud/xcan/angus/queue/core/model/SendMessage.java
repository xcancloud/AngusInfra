package cloud.xcan.angus.queue.core.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendMessage {

  private String topic;
  private String partitionKey;
  private String payload;
  private String headers;
  private Integer priority;
  private Instant visibleAt;
  private String idempotencyKey;
  private Integer maxAttempts;
  private Integer numPartitions;
}

