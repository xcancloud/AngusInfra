package cloud.xcan.sdf.api.message.http;

import static cloud.xcan.sdf.api.ApiConstant.ECode.SYSTEM_ERROR_CODE;
import static cloud.xcan.sdf.api.ExceptionLevel.URGENT;
import static cloud.xcan.sdf.api.message.http.ServiceUnavailable.M.SERVICE_UNAVAILABLE;
import static cloud.xcan.sdf.api.message.http.ServiceUnavailable.M.SERVICE_UNAVAILABLE_KEY;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import lombok.Getter;
import lombok.ToString;

/**
 * Gateway service unavailable exception class, which will be handled by global exception and return
 * http status 503.
 */
@Getter
@ToString
public class ServiceUnavailable extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public ServiceUnavailable() {
    this(SERVICE_UNAVAILABLE, null, SERVICE_UNAVAILABLE_KEY, URGENT);
  }

  public ServiceUnavailable(String message, Object[] args, String eKey,
      ExceptionLevel level) {
    super(message, EventType.SYSTEM, level, eKey);
    this.code = SYSTEM_ERROR_CODE;
    this.msg = message;
    this.args = args;
  }

  public static ServiceUnavailable of(String message) {
    return new ServiceUnavailable(message, null, SERVICE_UNAVAILABLE_KEY, URGENT);
  }

  public static ServiceUnavailable of(ExceptionLevel level) {
    return new ServiceUnavailable(SERVICE_UNAVAILABLE, null, SERVICE_UNAVAILABLE_KEY,
        level);
  }

  public static ServiceUnavailable of(String message, Object[] agrs) {
    return new ServiceUnavailable(message, agrs, SERVICE_UNAVAILABLE_KEY, URGENT);
  }

  public static ServiceUnavailable of(String message, Object[] agrs, String eKey) {
    return new ServiceUnavailable(message, agrs, eKey, URGENT);
  }

  public static ServiceUnavailable of(String message, ExceptionLevel level) {
    return new ServiceUnavailable(message, null, SERVICE_UNAVAILABLE_KEY, level);
  }

  public static ServiceUnavailable of(String message, Object[] agrs, String eKey,
      ExceptionLevel level) {
    return new ServiceUnavailable(message, agrs, eKey, level);
  }

  @Override
  public boolean is5xxException() {
    return true;
  }

  public interface M {

    String SERVICE_UNAVAILABLE = "xcm.service.unavailable";
    String SERVICE_UNAVAILABLE_T = "xcm.service.unavailable.t";
    String SERVICE_UNAVAILABLE_KEY = "service_unavailable";

    String ACCESS_TOO_FREQUENT = "ACCESS_TOO_FREQUENT";
    String ACCESS_TOO_FREQUENT_KEY = "access_too_frequent";

  }

}
