package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;


@Getter
public enum FileType implements EnumMessage<String> {
  FILE, DIRECTORY;

  @Override
  public String getValue() {
    return this.name();
  }
}
