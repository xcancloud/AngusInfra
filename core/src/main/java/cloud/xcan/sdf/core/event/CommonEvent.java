package cloud.xcan.sdf.core.event;


import cloud.xcan.sdf.core.event.source.EventContent;
import lombok.ToString;

@ToString(callSuper = true)
public class CommonEvent extends AbstractEvent<EventContent> {

  /**
   * Constructs a empty SimpleEvent.
   */
  public CommonEvent() {
    super();
  }

  /**
   * Create a new {@code CommonEvent}.
   *
   * @param source the object on which the event initially occurred or with which the event is
   *               associated (never {@code null})
   */
  public CommonEvent(EventContent source) {
    super(source, source.getType(), source.getCode(), source.getDescription(),
        source.getTimestamp(), source.getExt());
  }

}
