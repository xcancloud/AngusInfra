package cloud.xcan.angus.spec.http;

import cloud.xcan.angus.spec.experimental.Value;

public enum HttpMethod implements Value<String> {
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

}
