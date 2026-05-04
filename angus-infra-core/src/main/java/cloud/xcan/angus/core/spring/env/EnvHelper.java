package cloud.xcan.angus.core.spring.env;

import static cloud.xcan.angus.core.spring.env.AbstractEnvLoader.envs;
import static org.apache.commons.lang3.StringUtils.isBlank;

import cloud.xcan.angus.spec.setting.SystemSetting;
import cloud.xcan.angus.spec.setting.SystemSettingUtils;
import cloud.xcan.angus.spec.utils.EnumUtils;

public final class EnvHelper {

  private EnvHelper() {
  }

  private record KeySetting(String key) implements SystemSetting {

    @Override
    public String property() {
      return key;
    }

    @Override
    public String environmentVariable() {
      return key;
    }

    @Override
    public String defaultValue() {
      return null;
    }
  }

  public static int getInt(String key) {
    return parseInt(getString(key), 0);
  }

  public static int getInt(String key, int defaultValue) {
    return parseInt(getString(key), defaultValue);
  }

  public static long getLong(String key) {
    return parseLong(getString(key), 0L);
  }

  public static long getLong(String key, long defaultValue) {
    return parseLong(getString(key), defaultValue);
  }

  public static boolean getBoolean(String key) {
    String value = getString(key);
    return !isBlank(value) && Boolean.parseBoolean(value.trim());
  }

  public static boolean getBoolean(String key, boolean defaultValue) {
    String value = getString(key);
    return isBlank(value) ? defaultValue : Boolean.parseBoolean(value.trim());
  }

  public static <T extends Enum<T>> T getEnum(String key, Class<T> enumClz, T defaultValue) {
    String value = getString(key);
    if (isBlank(value)) {
      return defaultValue;
    }
    try {
      T parsed = EnumUtils.valueOf(enumClz, value.trim());
      return parsed != null ? parsed : defaultValue;
    } catch (IllegalArgumentException ex) {
      return defaultValue;
    }
  }

  public static String getString(String key, String defaultValue) {
    String value = getString(key);
    return isBlank(value) ? defaultValue : value;
  }

  /**
   * Resolves a setting in order: JVM system property, OS environment variable, then values loaded
   * from env files ({@link AbstractEnvLoader#envs}).
   */
  public static String getString(String key) {
    String value = SystemSettingUtils.resolveSetting(new KeySetting(key)).orElse(null);
    if (isBlank(value)) {
      value = envs.getProperty(key);
    }
    return value;
  }

  private static int parseInt(String raw, int defaultValue) {
    if (isBlank(raw)) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }

  private static long parseLong(String raw, long defaultValue) {
    if (isBlank(raw)) {
      return defaultValue;
    }
    try {
      return Long.parseLong(raw.trim());
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }
}
