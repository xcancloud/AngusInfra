package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;


public enum OpenStage implements Value<String> {

  SIGNUP,
  AUTH_PASSED,
  OPEN_SUCCESS;

  @Override
  public String getValue() {
    return this.name();
  }

}
