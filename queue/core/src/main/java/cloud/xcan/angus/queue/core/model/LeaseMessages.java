package cloud.xcan.angus.queue.core.model;

import java.util.Collection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaseMessages {

  private String topic;
  private Collection<Integer> partitions;
  private String owner;
  private Integer leaseSeconds;
  private Integer limit;
}

