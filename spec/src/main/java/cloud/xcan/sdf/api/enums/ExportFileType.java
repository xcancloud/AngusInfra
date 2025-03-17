package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author XiaoLong Liu
 */
@Getter
public enum ExportFileType implements EnumMessage<String> {

  JSON(".json"), CSV(".csv"), EXCEL(".xlsx");

  private final String fileSuffix;

  ExportFileType(String fileSuffix) {
    this.fileSuffix = fileSuffix;
  }

  @Override
  public String getValue() {
    return this.name();
  }

  @Override
  public String getMessage() {
    return this.name().toLowerCase();
  }
}
