package cloud.xcan.angus.spec.http;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

@EndpointRegister
public enum HttpMethod implements EnumValueMessage<String> {
  GET,
  HEAD,
  POST,
  PUT,
  PATCH,
  DELETE,
  OPTIONS,
  TRACE;

  @Override
  public String getValue() {
    return this.name();
  }

  @Override
  public String getMessage() {
    return this.name();
  }
}
