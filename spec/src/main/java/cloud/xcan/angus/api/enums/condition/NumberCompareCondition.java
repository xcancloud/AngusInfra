package cloud.xcan.angus.api.enums.condition;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * Condition applies to matches that may be numbers.
 */
@EndpointRegister
public enum NumberCompareCondition implements EnumMessage<String> {
  EQUAL,
  GREATER_THAN,
  GREATER_THAN_EQUAL,
  LESS_THAN,
  LESS_THAN_EQUAL;

  @Override
  public String getValue() {
    return this.name();
  }

}
