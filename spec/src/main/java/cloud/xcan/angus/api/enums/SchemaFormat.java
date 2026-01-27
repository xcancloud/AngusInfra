package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;


public enum SchemaFormat implements Value<String> {
  STRING, BOOLEAN, INTEGER, NUMBER, DATE, DATETIME, BINARY, BINARY_BASE64_TEXT, BINARY_URL,
  BYTEARRAY, FILE/*Equal BINARY*/, ARRAY, MAP/*Equal OBJECT*/, OBJECT, PASSWORD, EMAIL, UUID;

  @Override
  public String getValue() {
    return this.name();
  }

}
