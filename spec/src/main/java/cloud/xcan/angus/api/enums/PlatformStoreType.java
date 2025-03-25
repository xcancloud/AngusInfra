package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * Object storage support platform.
 *
 * @author XiaoLong Liu
 * @see PlatformStoreType#LOCAL Default privatized deployment storage method.
 * @see PlatformStoreType#AWS_S3 Default cloud deployment storage method.
 */
@EndpointRegister
public enum PlatformStoreType implements EnumMessage<String> {
  LOCAL, AWS_S3;

  @Override
  public String getValue() {
    return this.name();
  }

}
