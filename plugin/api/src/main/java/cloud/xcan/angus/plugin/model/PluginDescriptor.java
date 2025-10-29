package cloud.xcan.angus.plugin.model;

import java.util.List;
import java.util.Map;

/**
 * Plugin descriptor mapped from plugin.json
 */
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

    public PluginDescriptor() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPluginClass() {
        return pluginClass;
    }

    public void setPluginClass(String pluginClass) {
        this.pluginClass = pluginClass;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public String getMinSystemVersion() {
        return minSystemVersion;
    }

    public void setMinSystemVersion(String minSystemVersion) {
        this.minSystemVersion = minSystemVersion;
    }

    public List<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public void setRequiredPermissions(List<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

