package cloud.xcan.sdf.spec.experimental;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_LANGUAGE;

public enum Languages implements Value<String> {
  en, zh_CN;

  @Override
  public String getValue() {
    return this.name();
  }

  public Languages defaultLanguage() {
    return Languages.valueOf(DEFAULT_LANGUAGE);
  }
}
