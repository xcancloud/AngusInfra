package cloud.xcan.angus.core.event;


import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.core.event.source.UserOperation;
import lombok.ToString;

@ToString(callSuper = true)
public class OperationEvent extends AbstractEvent<UserOperation> {

  /**
   * Constructs a empty OperationEvent.
   */
  public OperationEvent() {
    super();
  }

  /**
   * Create a new {@code OperationEvent}.
   *
   * @param source the object on which the event initially occurred or with which the event is
   *               associated (never {@code null})
   */
  public OperationEvent(UserOperation source) {
    super(source, EventType.OPERATION.getValue(), source.getDescription(), source.getDescription(),
        source.getTimestamp(), source.getExt());
  }

}
