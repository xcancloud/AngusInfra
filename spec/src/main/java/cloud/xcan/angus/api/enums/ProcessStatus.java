package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum ProcessStatus implements Value<String> {
  PENDING, SUCCESS, FAILURE;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isPending() {
    return this.equals(PENDING);
  }

  public boolean isSuccess() {
    return this.equals(SUCCESS);
  }

  public boolean isFailure() {
    return this.equals(FAILURE);
  }
}
