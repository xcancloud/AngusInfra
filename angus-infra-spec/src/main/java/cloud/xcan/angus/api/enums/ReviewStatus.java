package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum ReviewStatus implements Value<String> {
  PENDING,
  PASSED,
  FAILED;

  @Override
  public String getValue() {
    return this.name();
  }

}
