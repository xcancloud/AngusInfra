package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.ValueObject;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum SchemaFormat implements ValueObject<SchemaFormat>, EnumValueMessage<String> {
  STRING, BOOLEAN, INTEGER, NUMBER, DATE, DATETIME, BINARY, BINARY_BASE64_TEXT, BINARY_URL,
  BYTEARRAY, FILE/*Equal BINARY*/, ARRAY, MAP/*Equal OBJECT*/, OBJECT, PASSWORD, EMAIL, UUID;

  @Override
  public String getValue() {
    return this.name();
  }

  @Override
  public String getMessage() {
    return this.name().toLowerCase();
  }
}
