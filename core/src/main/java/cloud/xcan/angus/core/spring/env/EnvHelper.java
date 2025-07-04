package cloud.xcan.angus.core.spring.env;

import static cloud.xcan.angus.core.spring.env.AbstractEnvLoader.envs;
import static org.apache.commons.lang3.StringUtils.isBlank;

import cloud.xcan.angus.spec.setting.SystemSetting;
import cloud.xcan.angus.spec.setting.SystemSettingUtils;
import cloud.xcan.angus.spec.utils.EnumUtils;

public class EnvHelper {

  public static int getInt(String key) {
    String value = getString(key);
    return isBlank(value) ? 0 : Integer.parseInt(value.trim());
  }

  public static int getInt(String key, int defaultValue) {
    String value = getString(key);
    return isBlank(value) ? defaultValue : Integer.parseInt(value.trim());
  }

  public static long getLong(String key) {
    String value = getString(key);
    return isBlank(value) ? 0 : Long.parseLong(value.trim());
  }

  public static long getLong(String key, long defaultValue) {
    String value = getString(key);
    return isBlank(value) ? defaultValue : Long.parseLong(value.trim());
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
    return isBlank(value) ? defaultValue : EnumUtils.valueOf(enumClz, value.trim());
  }

  public static String getString(String key, String defaultValue) {
    String value = getString(key);
    return isBlank(value) ? defaultValue : value;
  }

    /**
     * Resolve the value of this system setting, loading it from the System by checking:
     * <ol>
     *     <li>The system properties.</li>
     *     <li>The environment variables.</li>
     *     <li>The app env properties.</li>
     *     <li>The default value.</li>
     * </ol>
     */
  public static String getString(String key) {
    String value = SystemSettingUtils.resolveSetting(new SystemSetting() {
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
    }).orElse(null);

    if (isBlank(value)) {
      value = envs.getProperty(key);
    }
    return value;
  }
}
