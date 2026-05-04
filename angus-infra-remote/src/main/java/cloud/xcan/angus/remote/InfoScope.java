package cloud.xcan.angus.remote;


import cloud.xcan.angus.spec.experimental.Value;

public enum InfoScope implements Value<String> {
  BASIC, DETAIL;

  @Override
  public String getValue() {
    return this.name();
  }

}
