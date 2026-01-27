package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.Value;

public enum ResourceAuthType implements Value<String> {
  API, ACL;

  @Override
  public String getValue() {
    return this.name();
  }
}
