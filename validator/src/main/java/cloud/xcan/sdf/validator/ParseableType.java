package cloud.xcan.sdf.validator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ParseableType {
  TO_SHORT("Short"),
  TO_INT("Integer"),
  TO_LONG("Long"),
  TO_DOUBLE("Double"),
  TO_FLOAT("Float");
  @Getter
  private String friendlyName;
}
