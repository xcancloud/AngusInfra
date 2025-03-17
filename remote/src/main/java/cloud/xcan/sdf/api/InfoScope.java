package cloud.xcan.sdf.api;


import cloud.xcan.sdf.spec.experimental.Value;

public enum InfoScope implements Value<String> {
  BASIC, DETAIL;

  @Override
  public String getValue() {
    return this.name();
  }

}
