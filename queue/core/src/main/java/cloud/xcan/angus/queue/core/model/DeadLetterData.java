package cloud.xcan.angus.queue.core.model;

import java.time.Instant;
import lombok.Data;

@Data
public class DeadLetterData {

  private Long id;
  private String topic;
  private Integer partitionId;
  private String payload;
  private String headers;
  private Integer attempts;
  private String reason;
  private Instant createdAt;
}
