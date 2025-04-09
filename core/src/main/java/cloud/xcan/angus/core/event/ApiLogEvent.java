package cloud.xcan.angus.core.event;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.core.event.source.ApiLog;
import lombok.ToString;

@ToString(callSuper = true)
public class ApiLogEvent extends AbstractEvent<ApiLog> {

  /**
   * Constructs a empty CommonEvent.
   */
  public ApiLogEvent() {
    super();
  }

  /**
   * Create a new {@code CommonEvent}.
   *
   * @param source the object on which the event initially occurred or with which the event is
   *               associated (never {@code null})
   */
  public ApiLogEvent(ApiLog source) {
    super(source, EventType.API.getValue(), source.getCode(), source.getDescription(),
        source.getRequestDate(), null);
  }

}
