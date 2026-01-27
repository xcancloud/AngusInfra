package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

@Deprecated // 使用GM授权主体枚举代替
public enum AuthObjectType implements Value<String> {
  USER, DEPT, GROUP;

  @Override
  public String getValue() {
    return this.name();
  }
}
