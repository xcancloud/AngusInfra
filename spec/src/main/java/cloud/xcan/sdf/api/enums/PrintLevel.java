package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.ValueObject;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

/**
 * Controls the print level of logging.
 */
@EndpointRegister
public enum PrintLevel implements ValueObject<PrintLevel>, EnumValueMessage<String> {
  /**
   * No logging.
   */
  NONE,
  /**
   * Log only the request method and URL and the response status code and execution time.
   */
  BASIC,
  /**
   * Log the basic information along with request and response headers.
   */
  HEADERS,
  /**
   * Log the headers, body, and metadata for both requests and responses.
   */
  FULL;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isNone(){
    return this.equals(NONE);
  }

  public boolean isFull(){
    return this.equals(FULL);
  }
}
