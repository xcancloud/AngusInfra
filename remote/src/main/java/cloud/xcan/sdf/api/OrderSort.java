package cloud.xcan.sdf.api;


import cloud.xcan.sdf.spec.experimental.Value;

public enum OrderSort implements Value<String> {
  ASC, DESC;

  @Override
  public String getValue() {
    return this.name();
  }

}
