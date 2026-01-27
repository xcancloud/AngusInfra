package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum FileResourceType implements Value<String> {
  SPACE, DIRECTORY, FILE;

  @Override
  public String getValue() {
    return this.name();
  }
}
