package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum EnabledFlag implements EnumMessage<Boolean> {
  TRUE, FALSE;

  @Override
  public Boolean getValue() {
    return Boolean.valueOf(this.name());
  }

}
