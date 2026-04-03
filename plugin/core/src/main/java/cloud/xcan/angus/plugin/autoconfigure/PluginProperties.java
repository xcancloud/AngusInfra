package cloud.xcan.angus.plugin.autoconfigure;

import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Configuration holder for plugin system settings.
 * <p>
 * This is intentionally a simple POJO (no Spring annotations here) so it can be used by the starter
 * module which will bind configuration properties.
 * <p>
 * Plugin jar directory and runtime data directory are <strong>not</strong> configurable via
 * {@code application.yml}: they are always derived from {@link SpringAppDirUtils#getPluginDir()} and
 * {@link SpringAppDirUtils#getBizDataDir(String)} with {@code "plugin-data"}.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PluginProperties {

  /**
   * Storage backend used to persist plugins. When 'JPA' is selected the starter module should
   * provide a JPA-backed PluginStore implementation; otherwise disk storage is used.
   */
  public enum StorageType {
    DISK,
    JPA
  }

  // -------------------- Storage & paths --------------------

  /**
   * Storage type for plugins (disk or jpa). Default: DISK.
   */
  private StorageType storageType = StorageType.DISK;

  /**
   * Directory where plugin jars are stored when using disk storage. Fixed at runtime from
   * {@link SpringAppDirUtils#getPluginDir()}; not bindable from configuration.
   */
  @Setter(AccessLevel.NONE)
  private String directory;

  /**
   * Directory where plugin runtime data should be stored. Fixed at runtime from
   * {@link SpringAppDirUtils#getBizDataDir(String)} with {@code "plugin-data"}; not bindable from
   * configuration.
   */
  @Setter(AccessLevel.NONE)
  private String dataDirectory;

  public PluginProperties() {
    SpringAppDirUtils appDirs = new SpringAppDirUtils();
    this.directory = appDirs.getPluginDir();
    this.dataDirectory = appDirs.getBizDataDir("plugin-data");
  }

  /**
   * Builds properties with explicit paths for unit tests. Production code should use the no-arg
   * constructor.
   */
  public static PluginProperties forTesting(String directory, String dataDirectory) {
    PluginProperties p = new PluginProperties();
    p.directory = directory;
    p.dataDirectory = dataDirectory;
    return p;
  }

  /**
   * For unit tests that need to change paths after construction. Not used by Spring configuration
   * binding.
   */
  public void replacePathsForTesting(String directory, String dataDirectory) {
    this.directory = directory;
    this.dataDirectory = dataDirectory;
  }

  // -------------------- Lifecycle & upload --------------------

  /**
   * Whether plugins should be auto-loaded on startup.
   */
  private boolean autoLoad = true;

  /**
   * Maximum upload size for plugin artifacts (bytes). Default 50MB.
   */
  private long maxUploadSize = 52_428_800L; // 50 * 1024 * 1024 approx

  // -------------------- Management API --------------------

  /**
   * Whether the management API (plugin admin endpoints) is enabled.
   */
  private boolean enableManagementApi = true;

  /**
   * Prefix for management API endpoints.
   */
  private String managementApiPrefix = "/api/plugins";

  // -------------------- Security --------------------

  /**
   * Whether a security check is performed for plugin actions.
   */
  private boolean enableSecurityCheck = true;

  /**
   * Allowed sources for plugin uploads (e.g. trusted hosts). '*' means any.
   */
  private String[] allowedSources = {"*"};

  // -------------------- Scanning & validation --------------------

  /**
   * Interval (ms) for scanning the plugin directory when hotReload is enabled.
   */
  private long scanInterval = 30_000L;

  /**
   * Validate plugin metadata on startup.
   */
  private boolean validateOnStartup = true;

  // -------------------- Plugin-specific configuration --------------------

  /**
   * Default configuration values applied to all plugins.
   */
  private Map<String, Object> defaultConfiguration = new HashMap<>();

  /**
   * Per-plugin configuration overrides: pluginId -> (key -> value).
   */
  private Map<String, Map<String, Object>> pluginConfigurations = new HashMap<>();

}
