package cloud.xcan.sdf.api.enums.condition;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * Condition applies to matches that may be numbers.
 */
@EndpointRegister
public enum NumberMatchCondition implements EnumMessage<String> {
  EQUAL,
  NOT_EQUAL,
  IS_EMPTY,
  NOT_EMPTY,
  GREATER_THAN,
  GREATER_THAN_EQUAL,
  LESS_THAN,
  LESS_THAN_EQUAL,
  REG_MATCH;

  @Override
  public String getValue() {
    return this.name();
  }

}