package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum NodeRole implements EnumMessage<String> {
  MANAGEMENT,
  CONTROLLER,
  EXECUTION,
  MOCK_SERVICE,
  APPLICATION;

  @Override
  public String getValue() {
    return this.name();
  }

}
