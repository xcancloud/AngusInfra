package cloud.xcan.sdf.core.event;


import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.core.event.source.UserOperation;
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
    super(source, EventType.OPERATION, source.getDescription(), source.getDescription(),
        source.getTimestamp(), source.getExt());
  }

}
