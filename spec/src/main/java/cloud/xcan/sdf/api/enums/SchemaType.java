package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.ValueObject;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

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
