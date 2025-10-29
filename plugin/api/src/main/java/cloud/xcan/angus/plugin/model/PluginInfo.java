package cloud.xcan.angus.plugin.model;

import java.time.LocalDateTime;
import java.util.List;

public class PluginInfo {
    private String id;
    private String name;
    private String version;
    private String description;
    private String author;
    private PluginState state;
    private LocalDateTime loadedAt;
    private LocalDateTime startedAt;
    private String pluginClass;
    private List<String> dependencies;
    private String type;
    private String apiPrefix;
    private Integer endpointCount;
    private String filePath;
    private Long fileSize;
    private Boolean enabled;
    private String homepage;
    private String license;
    private List<String> tags;

    public PluginInfo() {
    }

    // getters & setters
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

    public PluginState getState() {
        return state;
    }

    public void setState(PluginState state) {
        this.state = state;
    }

    public LocalDateTime getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(LocalDateTime loadedAt) {
        this.loadedAt = loadedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public Integer getEndpointCount() {
        return endpointCount;
    }

    public void setEndpointCount(Integer endpointCount) {
        this.endpointCount = endpointCount;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    // Fluent builder to support PluginInfo.builder() usage
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PluginInfo info = new PluginInfo();

        public Builder id(String id) {
            info.setId(id);
            return this;
        }

        public Builder name(String name) {
            info.setName(name);
            return this;
        }

        public Builder version(String version) {
            info.setVersion(version);
            return this;
        }

        public Builder description(String description) {
            info.setDescription(description);
            return this;
        }

        public Builder author(String author) {
            info.setAuthor(author);
            return this;
        }

        public Builder state(PluginState state) {
            info.setState(state);
            return this;
        }

        public Builder loadedAt(LocalDateTime loadedAt) {
            info.setLoadedAt(loadedAt);
            return this;
        }

        public Builder startedAt(LocalDateTime startedAt) {
            info.setStartedAt(startedAt);
            return this;
        }

        public Builder pluginClass(String pluginClass) {
            info.setPluginClass(pluginClass);
            return this;
        }

        public Builder dependencies(List<String> dependencies) {
            info.setDependencies(dependencies);
            return this;
        }

        public Builder type(String type) {
            info.setType(type);
            return this;
        }

        public Builder apiPrefix(String apiPrefix) {
            info.setApiPrefix(apiPrefix);
            return this;
        }

        public Builder endpointCount(Integer endpointCount) {
            info.setEndpointCount(endpointCount);
            return this;
        }

        public Builder filePath(String filePath) {
            info.setFilePath(filePath);
            return this;
        }

        public Builder fileSize(Long fileSize) {
            info.setFileSize(fileSize);
            return this;
        }

        public Builder enabled(Boolean enabled) {
            info.setEnabled(enabled);
            return this;
        }

        public Builder homepage(String homepage) {
            info.setHomepage(homepage);
            return this;
        }

        public Builder license(String license) {
            info.setLicense(license);
            return this;
        }

        public Builder tags(List<String> tags) {
            info.setTags(tags);
            return this;
        }

        public PluginInfo build() {
            return info;
        }
    }
}

