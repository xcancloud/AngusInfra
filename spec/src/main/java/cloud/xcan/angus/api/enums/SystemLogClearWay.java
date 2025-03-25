package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
public enum SystemLogClearWay implements EnumMessage<String> {
  CLEAR_BEFORE_DAY, COMPRESSION_MOVE, CLEAR_BY_DISK_SIZE;

  @Override
  public String getValue() {
    return this.name();
  }
}
