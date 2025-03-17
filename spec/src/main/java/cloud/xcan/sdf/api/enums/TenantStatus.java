package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

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
