package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum EvalWorkloadMethod implements Value<String> {
  WORKING_HOURS, STORY_POINT;

  @Override
  public String getValue() {
    return this.name();
  }
}
