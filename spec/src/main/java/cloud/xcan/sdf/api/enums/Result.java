package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum Result implements EnumMessage<String> {
  SUCCESS, FAIL;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isSuccess() {
    return this.equals(SUCCESS);
  }

  public boolean isFail() {
    return this.equals(FAIL);
  }

}
