package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.locale.EnumMessage;
import lombok.Getter;


@Getter
public enum FileType implements EnumMessage<String> {
  FILE, DIRECTORY;

  @Override
  public String getValue() {
    return this.name();
  }
}
