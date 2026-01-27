package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum ParamIn implements Value<String> {
  cookie,
  header,
  query,
  path;

  @Override
  public String getValue() {
    return this.name();
  }
}
