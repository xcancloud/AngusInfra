package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum NormalStatus implements EnumMessage<String> {
  NORMAL, EXCEPTION, UNKNOWN;

  @Override
  public String getValue() {
    return this.name();
  }
}
