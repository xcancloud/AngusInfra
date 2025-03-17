package cloud.xcan.sdf.api.enums;


import cloud.xcan.sdf.spec.locale.EnumMessage;

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
