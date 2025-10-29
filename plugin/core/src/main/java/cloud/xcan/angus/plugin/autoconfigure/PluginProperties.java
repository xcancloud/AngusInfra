package cloud.xcan.angus.plugin.autoconfigure;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PluginProperties {
    private boolean enabled = true;
    private String directory = "./plugins";
    private String dataDirectory = "./plugin-data";
    private boolean autoLoad = true;
    private boolean hotReload = true;
    private long maxUploadSize = 52428800L;

    private boolean enableManagementApi = true;
    private String managementApiPrefix = "/api/plugins";
    private boolean enableSecurityCheck = true;
    private String[] allowedSources = {"*"};
    private long scanInterval = 30000L;
    private boolean validateOnStartup = true;
    private Map<String, Object> defaultConfiguration = new HashMap<>();
    private Map<String, Map<String, Object>> pluginConfigurations = new HashMap<>();

}
