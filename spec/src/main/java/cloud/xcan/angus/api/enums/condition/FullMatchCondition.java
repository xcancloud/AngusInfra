package cloud.xcan.angus.api.enums.condition;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum FullMatchCondition implements EnumMessage<String> {
  EQUAL,
  NOT_EQUAL,
  IS_EMPTY,
  NOT_EMPTY,
  IS_NULL,
  NOT_NULL,
  GREATER_THAN,
  GREATER_THAN_EQUAL,
  LESS_THAN,
  LESS_THAN_EQUAL,
  CONTAIN,
  NOT_CONTAIN,
  REG_MATCH,
  XPATH_MATCH,
  JSON_PATH_MATCH;

  @Override
  public String getValue() {
    return this.name();
  }

}
