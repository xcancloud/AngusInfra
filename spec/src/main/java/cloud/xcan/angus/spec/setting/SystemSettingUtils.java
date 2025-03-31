package cloud.xcan.angus.spec.setting;


import static cloud.xcan.angus.spec.utils.ObjectUtils.firstPresent;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * A set of static utility methods for shared code in {@link SystemSetting}.
 */
@Slf4j
public final class SystemSettingUtils {

  private SystemSettingUtils() {
  }

  /**
   * Resolve the value of this system setting, loading it from the System by checking:
   * <ol>
   *     <li>The system properties.</li>
   *     <li>The environment variables.</li>
   *     <li>The default value.</li>
   * </ol>
   */
  public static Optional<String> resolveSetting(SystemSetting setting) {
    return firstPresent(resolveProperty(setting), () -> resolveEnvironmentVariable(setting),
        () -> resolveDefault(setting))
        .map(String::trim);
  }

  /**
   * Resolve the value of this system setting, loading it from the System by checking:
   * <ol>
   *     <li>The system properties.</li>
   *     <li>The environment variables.</li>
   * </ol>
   * <p>
   * This is similar to {@link #resolveSetting(SystemSetting)} but does not fall back to the default value if neither
   * the environment variable or system property value are present.
   */
  public static Optional<String> resolveNonDefaultSetting(SystemSetting setting) {
    return firstPresent(resolveProperty(setting), () -> resolveEnvironmentVariable(setting))
        .map(String::trim);
  }

  /**
   * Attempt to load this setting from the system properties.
   */
  private static Optional<String> resolveProperty(SystemSetting setting) {
    // CHECKSTYLE:OFF - This is the only place we're allowed to use System.getProperty
    return Optional.ofNullable(setting.property()).map(System::getProperty);
    // CHECKSTYLE:ON
  }

  /**
   * Attempt to load this setting from the environment variables.
   */
  public static Optional<String> resolveEnvironmentVariable(SystemSetting setting) {
    return resolveEnvironmentVariable(setting.environmentVariable());
  }

  /**
   * Attempt to load a key from the environment variables.
   */
  public static Optional<String> resolveEnvironmentVariable(String key) {
    try {
      return Optional.ofNullable(System.getenv(key));
    } catch (SecurityException e) {
      log.debug(
          "Unable to load the environment variable '{}' because the security manager did not allow the SDK"
              + " to read this system property. This setting will be assumed to be null", key, e);
      return Optional.empty();
    }
  }

  /**
   * Load the default value from the setting.
   */
  private static Optional<String> resolveDefault(SystemSetting setting) {
    return Optional.ofNullable(setting.defaultValue());
  }

  /**
   * Convert a string to boolean safely (as opposed to the less strict
   * {@link Boolean#parseBoolean(String)}). If a customer specifies a boolean value it should be
   * "true" or "false" (case insensitive) or an exception will be thrown.
   */
  public static Boolean safeStringToBoolean(SystemSetting setting, String value) {
    if ("true".equalsIgnoreCase(value)) {
      return true;
    } else if ("false".equalsIgnoreCase(value)) {
      return false;
    }

    throw new IllegalStateException(
        "Environment variable '" + setting.environmentVariable() + "' or system property '" +
            setting.property() + "' was defined as '" + value
            + "', but should be 'false' or 'true'");
  }


}
