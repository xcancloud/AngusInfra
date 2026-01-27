package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum Enabled implements Value<Boolean> {
  TRUE, FALSE;

  @Override
  public Boolean getValue() {
    return Boolean.valueOf(this.name());
  }

}
