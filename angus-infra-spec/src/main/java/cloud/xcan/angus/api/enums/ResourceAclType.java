package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.Value;


public enum ResourceAclType implements Value<String> {
  ALL, ONLY_READ, ONLY_WRITE, READ_WRITE, DELETE;

  @Override
  public String getValue() {
    return this.name();
  }
}
