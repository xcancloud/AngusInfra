package cloud.xcan.angus.remote.message;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.spec.locale.MessageHolder;
import java.util.Objects;

/**
 * Custom exception base class, users can define exception classes based on this class to meet their
 * own business needs, including exception code, prompt information, exception key, and exception
 * printLevel.
 */
public abstract class AbstractResultMessageException extends RuntimeException implements
    ResultMessage {

  private EventType type;
  private String eKey;
  private ExceptionLevel level;

  //  public AbstractResultMessageException() {
  //    super();
  //  }

  public AbstractResultMessageException(String msg, EventType type) {
    this(msg, type, ExceptionLevel.WARNING, null, null);
  }

  public AbstractResultMessageException(String msg, EventType type, ExceptionLevel level) {
    this(msg, type, level, null, null);
  }

  public AbstractResultMessageException(String msg, EventType type, ExceptionLevel level,
      String eKey) {
    this(msg, type, level, eKey, null);
  }

  public AbstractResultMessageException(String msg, EventType type, ExceptionLevel level,
      Throwable cause) {
    this(msg, type, level, null, cause);
  }

  public AbstractResultMessageException(String msg, EventType type, ExceptionLevel level,
      String eKey, Throwable cause) {
    super(msg, cause);
    this.type = type;
    this.eKey = eKey;
    this.level = level;
  }

  public AbstractResultMessageException(Throwable cause) {
    super(cause);
  }

  public EventType getType() {
    return this.type;
  }

  public String getEKey() {
    return this.eKey;
  }

  public ExceptionLevel getLevel() {
    return this.level;
  }

  @Override
  public String getCauseMessage() {
    return Objects.nonNull(getCause()) ? getCause().getMessage() : null;
  }

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public String getMessage() {
    return MessageHolder.message(super.getMessage(), getArgs());
  }

  @Override
  public String toString() {
    return "AbstractResultMessageException{" +
        "type=" + type +
        ", code='" + getCode() + '\'' +
        ", message='" + getMessage() + '\'' +
        ", eKey='" + eKey + '\'' +
        ", level=" + level +
        '}';
  }
}
