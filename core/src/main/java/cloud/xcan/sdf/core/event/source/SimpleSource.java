package cloud.xcan.sdf.core.event.source;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.core.event.EventSource;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
@ToString
@Accessors(chain = true)
public class SimpleSource implements EventSource {

  private EventType type;
  private String code;
  private String description;
  @JsonFormat(pattern = DATE_FMT)
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime timestamp;

  private Map<String, Object> ext;

  public SimpleSource() {
    this(null, null, null);
  }

  public SimpleSource(EventType type, String code, String description) {
    this(type, code, description, null, null);
  }

  public SimpleSource(EventType type, String code, String description, LocalDateTime timestamp) {
    this(type, code, description, timestamp, null);
  }

  public SimpleSource(EventType type, String code, String description,
      LocalDateTime timestamp, Map<String, Object> ext) {
    this.type = type;
    this.code = code;
    this.description = description;
    this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
    this.ext = ext;
  }
}
