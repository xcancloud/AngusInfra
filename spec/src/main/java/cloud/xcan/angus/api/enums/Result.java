package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum Result implements Value<String> {
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
