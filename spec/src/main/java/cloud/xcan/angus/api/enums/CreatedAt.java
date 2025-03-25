package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;


@EndpointRegister
public enum CreatedAt implements EnumMessage<String> {
  NOW, AT_SOME_DATE, PERIODICALLY;

  @Override
  public String getValue() {
    return this.name();
  }

}
