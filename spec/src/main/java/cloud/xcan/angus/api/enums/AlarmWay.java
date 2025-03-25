package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum AlarmWay implements EnumMessage<String> {

  EMAIL,
  SMS;

  @Override
  public String getValue() {
    return this.name();
  }

}
