package cloud.xcan.sdf.api.message.http;

import static cloud.xcan.sdf.api.ApiConstant.ECode.SYSTEM_ERROR_CODE;
import static cloud.xcan.sdf.api.ExceptionLevel.URGENT;
import static cloud.xcan.sdf.api.message.http.GatewayTimeout.M.GATEWAY_TIMEOUT;
import static cloud.xcan.sdf.api.message.http.GatewayTimeout.M.GATEWAY_TIMEOUT_KEY;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import lombok.Getter;
import lombok.ToString;


/**
 * Gateway timeout exception class, which will be handled by global exception and return http status
 * 504.
 */
@Getter
@ToString
public class GatewayTimeout extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public GatewayTimeout() {
    this(GATEWAY_TIMEOUT, null, GATEWAY_TIMEOUT_KEY, URGENT);
  }

  public GatewayTimeout(String message, Object[] args, String eKey, ExceptionLevel level) {
    super(message, EventType.SYSTEM, level, eKey);
    this.code = SYSTEM_ERROR_CODE;
    this.msg = message;
    this.args = args;
  }

  public static GatewayTimeout of(String message) {
    return new GatewayTimeout(message, null, GATEWAY_TIMEOUT_KEY, URGENT);
  }

  public static GatewayTimeout of(ExceptionLevel level) {
    return new GatewayTimeout(GATEWAY_TIMEOUT, null, GATEWAY_TIMEOUT_KEY, level);
  }

  public static GatewayTimeout of(String message, Object[] agrs) {
    return new GatewayTimeout(message, agrs, GATEWAY_TIMEOUT_KEY, URGENT);
  }

  public static GatewayTimeout of(String message, Object[] agrs, String eKey) {
    return new GatewayTimeout(message, agrs, eKey, URGENT);
  }

  public static GatewayTimeout of(String message, ExceptionLevel level) {
    return new GatewayTimeout(message, null, GATEWAY_TIMEOUT_KEY, level);
  }

  public static GatewayTimeout of(String message, Object[] agrs, String eKey,
      ExceptionLevel level) {
    return new GatewayTimeout(message, agrs, eKey, level);
  }

  @Override
  public boolean is5xxException() {
    return true;
  }

  public interface M {

    String GATEWAY_TIMEOUT = "xcm.gateway.timeout";
    String GATEWAY_TIMEOUT_KEY = "gateway_timeout";

  }

}
