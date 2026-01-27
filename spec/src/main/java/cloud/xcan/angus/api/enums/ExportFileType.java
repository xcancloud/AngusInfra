package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;


public enum ExportFileType implements Value<String> {

  JSON(".json"), CSV(".csv"), EXCEL(".xlsx");

  ExportFileType(String fileSuffix) {
  }

  @Override
  public String getValue() {
    return this.name();
  }

}
