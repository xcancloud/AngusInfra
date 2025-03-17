package cloud.xcan.sdf.api.message.http;

import static cloud.xcan.sdf.api.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.sdf.api.ExceptionLevel.IGNORABLE;
import static cloud.xcan.sdf.api.message.http.MethodNotSupported.M.METHOD_NOT_ALLOWED;
import static cloud.xcan.sdf.api.message.http.MethodNotSupported.M.METHOD_NOT_ALLOWED_KEY;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import lombok.Getter;
import lombok.ToString;


/**
 * HttpMethod not supported exception class, which will be handled by global exception and return http
 * status 405.
 * <p>
 * Supported request methods：GET、POST、DELETE、PUT、PATCH、HEAD
 */
@Getter
@ToString
public class MethodNotSupported extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;
  private final String notSupportedMethod;

  public MethodNotSupported() {
    this(METHOD_NOT_ALLOWED, null, "", METHOD_NOT_ALLOWED_KEY, IGNORABLE);
  }

  private MethodNotSupported(String message, Object[] args, String notSupportedMethod,
      String eKey, ExceptionLevel level) {
    super(message, EventType.PROTOCOL, level, eKey);
    this.code = PROTOCOL_ERROR_CODE;
    this.msg = message;
    this.args = args;
    this.notSupportedMethod = notSupportedMethod;
  }

  public static MethodNotSupported of(String message) {
    return new MethodNotSupported(message, null, null, METHOD_NOT_ALLOWED_KEY, IGNORABLE);
  }

  public static MethodNotSupported of(String message, Object[] agrs) {
    return new MethodNotSupported(message, agrs, null, METHOD_NOT_ALLOWED_KEY, IGNORABLE);
  }

  public static MethodNotSupported of(String message, Object[] agrs,
      String notSupportedMethod, String eKey) {
    return new MethodNotSupported(message, agrs, notSupportedMethod, eKey, IGNORABLE);
  }

  public static MethodNotSupported of(String message, Object[] agrs,
      String notSupportedMethod) {
    return new MethodNotSupported(message, agrs, notSupportedMethod, METHOD_NOT_ALLOWED_KEY,
        IGNORABLE);
  }

  public static MethodNotSupported of(String message, String notSupportedMethod) {
    return new MethodNotSupported(message, null, notSupportedMethod, METHOD_NOT_ALLOWED_KEY,
        IGNORABLE);
  }

  public static MethodNotSupported of(String message, String notSupportedMethod,
      String eKey) {
    return new MethodNotSupported(message, null, notSupportedMethod, eKey, IGNORABLE);
  }

  public static MethodNotSupported of(String message, String notSupportedMethod,
      String eKey, ExceptionLevel level) {
    return new MethodNotSupported(message, null, notSupportedMethod, eKey, level);
  }

  public static MethodNotSupported of(String message, Object[] agrs,
      String notSupportedMethod, String eKey, ExceptionLevel level) {
    return new MethodNotSupported(message, agrs, notSupportedMethod, eKey, level);
  }

  @Override
  public boolean is4xxException() {
    return true;
  }

  public interface M {

    String METHOD_NOT_ALLOWED = "xcm.httpMethod.not.allowed";
    String METHOD_NOT_ALLOWED_T = "xcm.httpMethod.not.allowed.t";
    String METHOD_NOT_ALLOWED_KEY = "method_not_supported";

  }

}
