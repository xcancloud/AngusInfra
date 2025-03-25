package cloud.xcan.angus.core.event;

import cloud.xcan.angus.api.enums.EventType;
import java.time.LocalDateTime;

public interface EventSource {

  EventType getType();

  String getCode();

  String getDescription();

  LocalDateTime getTimestamp();

}
