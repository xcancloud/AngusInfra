package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ProcessStatus implements EnumMessage<String> {
  PENDING, SUCCESS, FAILURE;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isPending() {
    return this.equals(PENDING);
  }

  public boolean isSuccess() {
    return this.equals(SUCCESS);
  }

  public boolean isFailure() {
    return this.equals(FAILURE);
  }
}
