package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum AuthStatus implements EnumMessage<String> {
  PENDING,
  PASSED,
  FAILURE;

  @Override
  public String getValue() {
    return this.name();
  }
}
