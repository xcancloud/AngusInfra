package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.Value;

public enum SystemLogClearWay implements Value<String> {
  CLEAR_BEFORE_DAY, COMPRESSION_MOVE, CLEAR_BY_DISK_SIZE;

  @Override
  public String getValue() {
    return this.name();
  }
}
