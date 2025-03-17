package cloud.xcan.sdf.core.event;

import cloud.xcan.sdf.api.enums.EventType;
import java.time.LocalDateTime;

public interface EventSource {

  EventType getType();

  String getCode();

  String getDescription();

  LocalDateTime getTimestamp();

}
