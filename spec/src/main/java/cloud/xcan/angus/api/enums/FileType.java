package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;


public enum FileType implements Value<String> {
  FILE, DIRECTORY;

  @Override
  public String getValue() {
    return this.name();
  }
}
