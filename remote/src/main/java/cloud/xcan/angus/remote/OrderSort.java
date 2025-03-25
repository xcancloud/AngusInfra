package cloud.xcan.angus.remote;


import cloud.xcan.angus.spec.experimental.Value;

public enum OrderSort implements Value<String> {
  ASC, DESC;

  @Override
  public String getValue() {
    return this.name();
  }

}
