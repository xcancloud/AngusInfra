package cloud.xcan.angus.plugin.autoconfigure;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration holder for plugin system settings.
 * <p>
 * This is intentionally a simple POJO (no Spring annotations here) so it can be used
 * by the starter module which will bind configuration properties.
 */
@Data
public class PluginProperties {

    /**
     * Storage backend used to persist plugins. When 'JPA' is selected the starter module
     * should provide a JPA-backed PluginStore implementation; otherwise disk storage is used.
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
     * Directory where plugin jars are stored when using disk storage.
     */
    private String directory = "./plugins";

    /**
     * Directory where plugin runtime data should be stored.
     */
    private String dataDirectory = "./plugin-data";

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
