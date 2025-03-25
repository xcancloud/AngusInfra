package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum EvalWorkloadMethod implements EnumMessage<String> {
  WORKING_HOURS, STORY_POINT;

  @Override
  public String getValue() {
    return this.name();
  }
}
