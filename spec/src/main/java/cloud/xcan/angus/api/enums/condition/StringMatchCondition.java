package cloud.xcan.angus.api.enums.condition;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * Condition applies to matches that may be characters and numbers.
 */
@EndpointRegister
public enum StringMatchCondition implements EnumMessage<String> {
  EQUAL,
  NOT_EQUAL,
  IS_EMPTY,
  NOT_EMPTY,
  CONTAIN,
  NOT_CONTAIN,
  REG_MATCH;

  @Override
  public String getValue() {
    return this.name();
  }

}
