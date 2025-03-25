package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum EnabledFlag implements EnumMessage<Boolean> {
  TRUE, FALSE;

  @Override
  public Boolean getValue() {
    return Boolean.valueOf(this.name());
  }

}
