package cloud.xcan.sdf.spec.locale;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_LANGUAGE;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Locale;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum SupportedLanguage implements EnumMessage<String> {
  en,
  zh_CN;

  @Override
  public String getValue() {
    return this.name();
  }

  public Locale toLocale() {
    switch (this) {
      case en:
        return Locale.ENGLISH;
      case zh_CN:
      default:
        return Locale.CHINA;
    }
  }

  public static boolean contain(String value) {
    if (ObjectUtils.isEmpty(value)) {
      return false;
    }
    for (SupportedLanguage language : SupportedLanguage.values()) {
      if (language.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  public static SupportedLanguage defaultLanguage() {
    return SupportedLanguage.valueOf(DEFAULT_LANGUAGE);
  }

  public static Locale safeLocale(Locale locale) {
    if (contain(locale.toString())) {
      return locale;
    }
    return Locale.SIMPLIFIED_CHINESE;
  }

  public static SupportedLanguage safeLanguage(Locale locale) {
    if (contain(locale.toString())) {
      return SupportedLanguage.valueOf(locale.toString());
    }
    return SupportedLanguage.zh_CN;
  }

  public static SupportedLanguage safeLanguage(String language) {
    if (contain(language)) {
      return SupportedLanguage.valueOf(language);
    }
    return SupportedLanguage.zh_CN;
  }
}
