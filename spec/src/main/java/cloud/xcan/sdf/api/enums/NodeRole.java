package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

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
