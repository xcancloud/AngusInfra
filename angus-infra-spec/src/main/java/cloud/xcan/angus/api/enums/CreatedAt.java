package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;


public enum CreatedAt implements Value<String> {
  NOW, AT_SOME_DATE, PERIODICALLY;

  @Override
  public String getValue() {
    return this.name();
  }

}
