package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum AlarmWay implements EnumMessage<String> {

  EMAIL,
  SMS;

  @Override
  public String getValue() {
    return this.name();
  }

}
