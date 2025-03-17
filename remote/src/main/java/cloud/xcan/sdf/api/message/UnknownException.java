package cloud.xcan.sdf.api.message;

import static cloud.xcan.sdf.api.ApiConstant.ECode.SYSTEM_ERROR_CODE;
import static cloud.xcan.sdf.api.ExceptionLevel.URGENT;
import static cloud.xcan.sdf.api.message.UnknownException.M.UNKNOWN_ERROR;
import static cloud.xcan.sdf.api.message.UnknownException.M.UNKNOWN_ERROR_KEY;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.enums.EventType;
import lombok.Getter;
import lombok.ToString;

/**
 * Unknown exception class, which will be handled by global exception and return http status 500.
 */
@Getter
@ToString
public class UnknownException extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public UnknownException() {
    this(UNKNOWN_ERROR, null, UNKNOWN_ERROR_KEY, URGENT);
  }

  private UnknownException(String message, Object[] args, String eKey, ExceptionLevel level) {
    this(message, args, eKey, level, null);
  }

  private UnknownException(String message, Object[] args, String eKey, ExceptionLevel level,
      Throwable cause) {
    super(message, EventType.SYSTEM, level, eKey, cause);
    this.code = SYSTEM_ERROR_CODE;
    this.msg = message;
    this.args = args;
  }

  public static UnknownException of(String message) {
    return new UnknownException(message, null, UNKNOWN_ERROR_KEY, URGENT);
  }

  public static UnknownException of(String message, Throwable cause) {
    return new UnknownException(message, null, UNKNOWN_ERROR_KEY, URGENT, cause);
  }

  public static UnknownException of(String message, Object[] args) {
    return new UnknownException(message, args, UNKNOWN_ERROR_KEY, URGENT);
  }

  public static UnknownException of(String message, Object[] args, Throwable cause) {
    return new UnknownException(message, args, UNKNOWN_ERROR_KEY, URGENT, cause);
  }

  public static UnknownException of(String message, String eKey) {
    return new UnknownException(message, null, eKey, URGENT);
  }

  public static UnknownException of(String message, String eKey, Throwable cause) {
    return new UnknownException(message, null, eKey, URGENT, cause);
  }

  public static UnknownException of(String message, Object[] args, String eKey) {
    return new UnknownException(message, args, eKey, URGENT);
  }

  public static UnknownException of(String message, Object[] args, String eKey, Throwable cause) {
    return new UnknownException(message, args, eKey, URGENT, cause);
  }

  public static UnknownException of(ExceptionLevel level) {
    return new UnknownException(UNKNOWN_ERROR, null, UNKNOWN_ERROR_KEY, level);
  }

  public static UnknownException of(ExceptionLevel level, Throwable cause) {
    return new UnknownException(UNKNOWN_ERROR, null, UNKNOWN_ERROR_KEY, level, cause);
  }

  public static UnknownException of(String message, ExceptionLevel level) {
    return new UnknownException(message, null, UNKNOWN_ERROR_KEY, level);
  }

  public static UnknownException of(String message, ExceptionLevel level, Throwable cause) {
    return new UnknownException(message, null, UNKNOWN_ERROR_KEY, level, cause);
  }

  public static UnknownException of(String message, Object[] args, ExceptionLevel level) {
    return new UnknownException(message, args, UNKNOWN_ERROR_KEY, level);
  }

  public static UnknownException of(String message, Object[] args, ExceptionLevel level,
      Throwable cause) {
    return new UnknownException(message, args, UNKNOWN_ERROR_KEY, level, cause);
  }

  public static UnknownException of(String message, String eKey, ExceptionLevel level) {
    return new UnknownException(message, null, eKey, level);
  }

  public static UnknownException of(String message, String eKey, ExceptionLevel level,
      Throwable cause) {
    return new UnknownException(message, null, eKey, level, cause);
  }

  public static UnknownException of(String message, Object[] args, String eKey,
      ExceptionLevel level) {
    return new UnknownException(message, args, eKey, level);
  }

  public static UnknownException of(String message, Object[] args, String eKey,
      ExceptionLevel level, Throwable cause) {
    return new UnknownException(message, args, eKey, level, cause);
  }

  @Override
  public boolean is5xxException() {
    return true;
  }

  @Override
  public String getCode() {
    return SYSTEM_ERROR_CODE;
  }

  public interface M {

    String UNKNOWN_ERROR = "xcm.unknown.error";
    String UNKNOWN_ERROR_KEY = "unknown_error";

  }
}
