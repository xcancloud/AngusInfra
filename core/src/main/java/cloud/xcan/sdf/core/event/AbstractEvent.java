package cloud.xcan.sdf.core.event;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.spec.EventObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
public abstract class AbstractEvent<T> extends EventObject<T> {

  private EventType type;

  private String code;

  private String description;

  @JsonFormat(pattern = DATE_FMT)
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime timestamp;

  /**
   * Non-generic field
   */
  private Map<String, Object> ext;

  /**
   * Constructs a empty SimpleEvent.
   */
  public AbstractEvent() {
    super();
  }

  /**
   * Create a new {@code ApplicationEvent}.
   *
   * @param source      the object on which the event initially occurred or with which the event is
   *                    associated (never {@code null})
   * @param type        the event type
   * @param code        the event name
   * @param description the event content
   * @param ext         the event ext
   */
  public AbstractEvent(T source, EventType type, String code, String description,
      LocalDateTime timestamp, Map<String, Object> ext) {
    super(source);
    this.type = type;
    this.code = code;
    this.description = description;
    this.timestamp = timestamp;
    this.ext = ext;
  }

}
