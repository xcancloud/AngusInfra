package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum TenantStatus implements EnumMessage<String> {
  DISABLED,
  CANCELLING,
  CANCELED,
  ENABLED;

  @Override
  public String getValue() {
    return this.name();
  }
}
