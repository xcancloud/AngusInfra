package cloud.xcan.angus.core.event;

import java.time.LocalDateTime;

public interface EventSource {

  String getType();

  String getCode();

  String getDescription();

  LocalDateTime getTimestamp();

}
