package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum SchemaType implements ValueObject<SchemaType>, EnumValueMessage<String> {
  ARRAY, STRING, BOOLEAN, INTEGER, OBJECT, NUMBER;

  @Override
  public String getValue() {
    return this.name();
  }

  @Override
  public String getMessage() {
    return this.name().toLowerCase();
  }
}
