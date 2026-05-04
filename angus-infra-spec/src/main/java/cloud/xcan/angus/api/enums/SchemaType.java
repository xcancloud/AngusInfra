package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;


public enum SchemaType implements Value<String> {
  ARRAY, STRING, BOOLEAN, INTEGER, OBJECT, NUMBER;

  @Override
  public String getValue() {
    return this.name();
  }

}
