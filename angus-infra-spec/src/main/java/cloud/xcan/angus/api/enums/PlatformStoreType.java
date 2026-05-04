package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

/**
 * Object storage multitenancy platform.
 *
 * @author XiaoLong Liu
 * @see PlatformStoreType#LOCAL Default privatized deployment storage method.
 * @see PlatformStoreType#AWS_S3 Default cloud deployment storage method.
 */
public enum PlatformStoreType implements Value<String> {
  LOCAL, AWS_S3;

  @Override
  public String getValue() {
    return this.name();
  }

}
