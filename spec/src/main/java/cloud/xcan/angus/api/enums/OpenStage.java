package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;


@EndpointRegister
@Getter
public enum OpenStage implements EnumMessage<String> {

  SIGNUP,
  AUTH_PASSED,
  OPEN_SUCCESS;

  @Override
  public String getValue() {
    return this.name();
  }

}
