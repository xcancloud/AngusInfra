package cloud.xcan.angus.spec.setting;

import java.util.Optional;

/**
 * An interface implemented by enums in other packages in order to define the system settings the
 * want loaded. An enum is expected to implement this interface, and then have values loaded from
 * the {@link System} using methods like {@link #getStringValue()}.
 */
public interface SystemSetting {

  /**
   * The system property of the setting (or null if there is no property for this setting).
   */
  String property();

  /**
   * The environment variable of the setting (or null if there is no environment variable for this
   * setting).
   */
  String environmentVariable();

  /**
   * The default value of the setting (or empty if there is no default). This value will be applied
   * if the customer did not specify a setting.
   */
  String defaultValue();

  /**
   * Attempt to load a system setting from {@link System#getProperty(String)} and
   * {@link System#getenv(String)}. This should be used in favor of those methods because the SDK
   * should support both methods of configuration.
   * <p>
   * {@link System#getProperty(String)} takes precedent over {@link System#getenv(String)} if both
   * are specified.
   *
   * @return The requested setting, or {@link Optional#empty()} if the values were not set, or the
   * security manager did not allow reading the setting.
   */
  default Optional<String> getStringValue() {
    return SystemSettingUtils.resolveSetting(this);
  }

  /**
   * Attempt to load a system setting from {@link System#getenv(String)}. This should NOT BE USED,
   * so checkstyle will complain about using it. The only reason this is made available is for when
   * we ABSOLUTELY CANNOT support a system property for a specific setting. That should be the
   * exception, NOT the rule. We should almost always provide a system property alternative for all
   * environment variables.
   *
   * @return The requested setting, or {@link Optional#empty()} if the values were not set, or the
   * security manager did not allow reading the setting.
   */
  static Optional<String> getStringValueFromEnvironmentVariable(String key) {
    return SystemSettingUtils.resolveEnvironmentVariable(key);
  }

  /**
   * Attempt to load a system setting from {@link System#getProperty(String)} and
   * {@link System#getenv(String)}. This should be used in favor of those methods because the SDK
   * should support both methods of configuration.
   * <p>
   * {@link System#getProperty(String)} takes precedent over {@link System#getenv(String)} if both
   * are specified.
   * <p>
   * Similar to {@link #getStringValue()}, but does not fall back to the default value.
   *
   * @return The requested setting, or {@link Optional#empty()} if the values were not set, or the
   * security manager did not allow reading the setting.
   */
  default Optional<String> getNonDefaultStringValue() {
    return SystemSettingUtils.resolveNonDefaultSetting(this);
  }

  /**
   * Load the requested system setting as per the documentation in {@link #getStringValue()},
   * throwing an exception if the value was not set and had no default.
   *
   * @return The requested setting.
   */
  default String getStringValueOrThrow() {
    return getStringValue().orElseThrow(() ->
        new IllegalStateException(
            "Either the environment variable " + environmentVariable() + " or the java"
                + "property " + property() + " must be set."));
  }

  /**
   * Attempt to load a system setting from {@link System#getProperty(String)} and
   * {@link System#getenv(String)}. This should be used in favor of those methods because the SDK
   * should support both methods of configuration.
   * <p>
   * The result will be converted to an integer.
   * <p>
   * {@link System#getProperty(String)} takes precedent over {@link System#getenv(String)} if both
   * are specified.
   *
   * @return The requested setting, or {@link Optional#empty()} if the values were not set, or the
   * security manager did not allow reading the setting.
   */
  default Optional<Integer> getIntegerValue() {
    return getStringValue().map(Integer::parseInt);
  }


  /**
   * Load the requested system setting as per the documentation in {@link #getIntegerValue()},
   * throwing an exception if the value was not set and had no default.
   *
   * @return The requested setting.
   */
  default Integer getIntegerValueOrThrow() {
    return Integer.parseInt(getStringValueOrThrow());
  }

  /**
   * Attempt to load a system setting from {@link System#getProperty(String)} and
   * {@link System#getenv(String)}. This should be used in favor of those methods because the SDK
   * should support both methods of configuration.
   * <p>
   * The result will be converted to a boolean.
   * <p>
   * {@link System#getProperty(String)} takes precedent over {@link System#getenv(String)} if both
   * are specified.
   *
   * @return The requested setting, or {@link Optional#empty()} if the values were not set, or the
   * security manager did not allow reading the setting.
   */
  default Optional<Boolean> getBooleanValue() {
    return getStringValue().map(value -> SystemSettingUtils.safeStringToBoolean(this, value));
  }

  /**
   * Load the requested system setting as per the documentation in {@link #getBooleanValue()},
   * throwing an exception if the value was not set and had no default.
   *
   * @return The requested setting.
   */
  default Boolean getBooleanValueOrThrow() {
    return getBooleanValue().orElseThrow(() ->
        new IllegalStateException(
            "Either the environment variable " + environmentVariable() + " or the java"
                + "property " + property() + " must be set."));
  }
}
