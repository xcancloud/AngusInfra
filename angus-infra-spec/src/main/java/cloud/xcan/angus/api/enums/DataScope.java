package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.Value;


public enum DataScope implements Value<String> {
  PUBLIC, CREATOR, DEPT, GROUP;

  @Override
  public String getValue() {
    return this.name();
  }

}
