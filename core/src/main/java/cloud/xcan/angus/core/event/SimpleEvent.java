package cloud.xcan.angus.core.event;


import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.core.event.source.SimpleSource;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.ToString;

@ToString(callSuper = true)
public class SimpleEvent extends AbstractEvent<SimpleSource> {

  /**
   * Constructs a empty SimpleEvent.
   */
  public SimpleEvent() {
    super();
  }

  /**
   * Create a new {@code SimpleSource}.
   *
   * @param source the object on which the event initially occurred or with which the event is
   *               associated (never {@code null})
   */
  public SimpleEvent(SimpleSource source) {
    super(source, EventType.OPERATION, source.getDescription(), source.getDescription(),
        source.getTimestamp(), source.getExt());
  }

  private SimpleEvent(Builder builder) {
    source = builder.source;
    setType(builder.type);
    setCode(builder.code);
    setDescription(builder.description);
    setTimestamp(builder.timestamp);
    setExt(builder.ext);
  }

  public static Builder newBuilder() {
    return new Builder();
  }


  public static final class Builder {

    private SimpleSource source;
    private EventType type;
    private String code;
    private String description;
    private LocalDateTime timestamp;
    private Map<String, Object> ext;

    private Builder() {
    }

    public Builder setSource(SimpleSource source) {
      this.source = source;
      return this;
    }

    public Builder setType(EventType type) {
      this.type = type;
      return this;
    }

    public Builder setCode(String code) {
      this.code = code;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setTimestamp(LocalDateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder setExt(Map<String, Object> ext) {
      this.ext = ext;
      return this;
    }

    public SimpleEvent build() {
      return new SimpleEvent(this);
    }
  }
}
