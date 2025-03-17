package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;


@EndpointRegister
public enum CreatedAt implements EnumMessage<String> {
  NOW, AT_SOME_DATE, PERIODICALLY;

  @Override
  public String getValue() {
    return this.name();
  }

}
