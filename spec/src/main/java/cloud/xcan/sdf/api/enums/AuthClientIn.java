package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum AuthClientIn implements EnumMessage<String> {
  QUERY_PARAMETER,
  BASIC_AUTH_HEADER,
  REQUEST_BODY;

  @Override
  public String getValue() {
    return this.name();
  }
}
