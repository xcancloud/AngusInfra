package cloud.xcan.angus.spec.setting;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.spec.utils.AppDirUtils;
import cloud.xcan.angus.spec.utils.EnumUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolve the value of this system setting, loading it from the System by checking:
 * <ol>
 *     <li>The system properties.</li>
 *     <li>The environment variables.</li>
 *     <li>The custom properties.</li>
 *     <li>The default properties.</li>
 *     <li>The default value.</li>
 * </ol>
 */
@Slf4j
public class AppSettingHelper {

  private static volatile Setting setting;

  public static Setting getSetting() {
    if (setting == null) {
      setting = new Setting();
    }
    return setting;
  }

  public static Setting getSetting(String defaultPropertiesFile, Class<?> loadResourceClass) {
    if (setting == null) {
      setting = new Setting(defaultPropertiesFile, null, loadResourceClass);
    }
    return setting;
  }

  public static Setting getSetting(String defaultPropertiesFile, String customPropertiesFile,
      Class<?> loadResourceClass) {
    if (setting == null) {
      setting = new Setting(defaultPropertiesFile, customPropertiesFile, loadResourceClass);
    }
    return setting;
  }

  /**
   * This method invalidates the property caches. So if a system property has been changed and the
   * effect of this change is to be seen then call reloadProperties() and then
   * getAsyncHttpClientConfig() to get the new property values.
   */
  public static void reloadProperties() {
    if (setting != null) {
      setting.reload();
    }
  }

  public static class Setting {

    public static final String DEFAULT_PROPERTIES = "xcan-default.properties";
    public static final String CUSTOM_PROPERTIES = "xcan.properties";

    private final ConcurrentHashMap<String, String> propsCache = new ConcurrentHashMap<>();

    private final AppDirUtils dir;
    private final Class<?> loadResourceClassClass;
    private final Properties defaultProperties;
    private final String customPropertiesFile;
    private volatile Properties customProperties;

    public Setting() {
      this(DEFAULT_PROPERTIES, CUSTOM_PROPERTIES);
    }

    public Setting(String defaultPropertiesFile, String customPropertiesFile) {
      this(defaultPropertiesFile, customPropertiesFile, null);
    }

    public Setting(String defaultPropertiesFile, String customPropertiesFile,
        Class<?> loadResourceClassClass) {
      this.dir = new AppDirUtils();
      this.loadResourceClassClass = nullSafe(loadResourceClassClass, getClass());
      this.defaultProperties = parsePropertiesFile(defaultPropertiesFile, false, true);
      this.customPropertiesFile = customPropertiesFile;
      this.customProperties = parsePropertiesFile(customPropertiesFile, true, false);
    }

    public void reload() {
      customProperties = parsePropertiesFile(customPropertiesFile, true, false);
      propsCache.clear();
    }

    private Properties parsePropertiesFile(String file, boolean custom, boolean required) {
      Properties props = new Properties();

      InputStream is = null;
      String confDir = getConfDir();
      if (confDir != null) {
        if (!confDir.endsWith(File.separator)) {
          confDir += File.separator;
        }
        Path path = Paths.get(confDir + file);
        if (custom && Files.exists(path)) {
          try {
            is = Files.newInputStream(path);
          } catch (IOException e) {
            log.error("Read {} exception", path.toString(), e);
          }
        }
      }

      if (is == null) {
        is = loadResourceClassClass.getResourceAsStream(file);

        if (is == null) {
          is = loadResourceClassClass.getClassLoader().getResourceAsStream(file);
        }
      }

      if (is != null) {
        try {
          props.load(is);
        } catch (IOException e) {
          throw new IllegalArgumentException("Can't parse setting file " + file, e);
        }
      } else if (required) {
        throw new IllegalArgumentException("Can't locate setting file " + file);
      }
      return props;
    }

    public String getConfDir() {
      return dir.getConfDir();
    }

    public AppDirUtils getDir() {
      return dir;
    }

    public int getInt(String key) {
      String value = getString(key);
      return isEmpty(value) ? 0 : Integer.parseInt(value.trim());
    }

    public int getInt(String key, int defaultValue) {
      String value = getString(key);
      return isEmpty(value) ? defaultValue : Integer.parseInt(value.trim());
    }

    public long getLong(String key) {
      String value = getString(key);
      return isEmpty(value) ? 0 : Long.parseLong(value.trim());
    }

    public long getLong(String key, long defaultValue) {
      String value = getString(key);
      return isEmpty(value) ? defaultValue : Long.parseLong(value.trim());
    }

    public boolean getBoolean(String key) {
      String value = getString(key);
      return !isEmpty(value) && Boolean.parseBoolean(value.trim());
    }

    public boolean getBoolean(String key, boolean defaultValue) {
      String value = getString(key);
      return isEmpty(value) ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClz, T defaultValue) {
      String value = getString(key);
      return isEmpty(value) ? defaultValue : EnumUtils.valueOf(enumClz, value.trim());
    }

    /**
     * Resolve the value of this system setting, loading it from the System by checking:
     * <ol>
     *     <li>The system properties.</li>
     *     <li>The environment variables.</li>
     *     <li>The custom properties.</li>
     *     <li>The default properties.</li>
     *     <li>The default value.</li>
     * </ol>
     */
    public String getString(String key) {
      return propsCache.computeIfAbsent(key, k -> {
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
        if (value == null) {
          value = customProperties.getProperty(k);
        }
        if (value == null) {
          value = defaultProperties.getProperty(k);
        }
        return value;
      });
    }

    public String getString(String key, String defaultValue) {
      String value = getString(key);
      return isEmpty(value) ? defaultValue : value.trim();
    }

    public String[] getStringArray(String key) {
      String s = getString(key);
      s = s.trim();
      if (s.isEmpty()) {
        return null;
      }
      String[] rawArray = s.split(",");
      String[] array = new String[rawArray.length];
      for (int i = 0; i < rawArray.length; i++) {
        array[i] = rawArray[i].trim();
      }
      return array;
    }
  }
}
