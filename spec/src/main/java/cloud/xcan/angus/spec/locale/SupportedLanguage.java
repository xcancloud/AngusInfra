package cloud.xcan.angus.spec.locale;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_LANGUAGE;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
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
    return switch (this) {
      case en -> Locale.ENGLISH;
      default -> Locale.CHINA;
    };
  }

  public static boolean contain(String value) {
    if (isEmpty(value)) {
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
