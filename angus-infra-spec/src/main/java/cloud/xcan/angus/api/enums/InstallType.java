package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum InstallType implements Value<String> {

  SHARED, STANDALONE;

  public boolean isShared() {
    return this.equals(SHARED);
  }

  public boolean isStandalone() {
    return this.equals(STANDALONE);
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
