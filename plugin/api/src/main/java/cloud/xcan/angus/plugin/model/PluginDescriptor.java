package cloud.xcan.angus.plugin.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Plugin descriptor mapped from plugin.json
 */
@Setter
@Getter
@NoArgsConstructor
public class PluginDescriptor {
    private String id;
    private String name;
    private String version;
    private String description;
    private String author;
    private String pluginClass;
    private List<String> dependencies;
    private List<String> libraries;
    private Map<String, Object> configuration;
    private String minSystemVersion;
    private List<String> requiredPermissions;
    private String homepage;
    private String license;
    private List<String> tags;

}

