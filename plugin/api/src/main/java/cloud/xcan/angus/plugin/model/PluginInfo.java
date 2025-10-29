package cloud.xcan.angus.plugin.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Simple value object describing a plugin instance as known to the host.
 * <p>
 * Contains metadata such as id/name/version, lifecycle timestamps, controller
 * endpoint counts and other optional fields useful for management UIs.
 */
@Setter
@Getter
public class PluginInfo {
    /**
     * Plugin identifier (unique)
     */
    private String id;
    /**
     * Human-readable name
     */
    private String name;
    /**
     * Plugin version, e.g. 1.0.0
     */
    private String version;
    /**
     * Short description of plugin
     */
    private String description;
    /**
     * Author or vendor
     */
    private String author;
    /**
     * Current lifecycle state
     */
    private PluginState state;
    /**
     * Time when plugin was loaded
     */
    private LocalDateTime loadedAt;
    /**
     * Time when plugin was started (may be null if not started)
     */
    private LocalDateTime startedAt;
    /**
     * Fully-qualified plugin class name
     */
    private String pluginClass;
    private List<String> dependencies;
    private String type;
    private String apiPrefix;
    /**
     * Number of REST endpoints exposed by the plugin (if applicable)
     */
    private Integer endpointCount;
    private String filePath;
    private Long fileSize;
    private Boolean enabled;
    private String homepage;
    private String license;
    private List<String> tags;

    public PluginInfo() {
    }

    /**
     * Obtain a new {@link Builder} instance to construct a {@link PluginInfo}
     * object.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PluginInfo info = new PluginInfo();

        /**
         * Set the unique identifier for the plugin.
         *
         * @param id the plugin ID
         * @return this builder instance
         */
        public Builder id(String id) {
            info.setId(id);
            return this;
        }

        /**
         * Set the human-readable name of the plugin.
         *
         * @param name the plugin name
         * @return this builder instance
         */
        public Builder name(String name) {
            info.setName(name);
            return this;
        }

        /**
         * Set the version of the plugin.
         *
         * @param version the plugin version
         * @return this builder instance
         */
        public Builder version(String version) {
            info.setVersion(version);
            return this;
        }

        /**
         * Set a short description for the plugin.
         *
         * @param description the plugin description
         * @return this builder instance
         */
        public Builder description(String description) {
            info.setDescription(description);
            return this;
        }

        /**
         * Set the author or vendor of the plugin.
         *
         * @param author the plugin author
         * @return this builder instance
         */
        public Builder author(String author) {
            info.setAuthor(author);
            return this;
        }

        /**
         * Set the current lifecycle state of the plugin.
         *
         * @param state the plugin state
         * @return this builder instance
         */
        public Builder state(PluginState state) {
            info.setState(state);
            return this;
        }

        /**
         * Set the time when the plugin was loaded.
         *
         * @param loadedAt the load time
         * @return this builder instance
         */
        public Builder loadedAt(LocalDateTime loadedAt) {
            info.setLoadedAt(loadedAt);
            return this;
        }

        /**
         * Set the time when the plugin was started.
         *
         * @param startedAt the start time
         * @return this builder instance
         */
        public Builder startedAt(LocalDateTime startedAt) {
            info.setStartedAt(startedAt);
            return this;
        }

        /**
         * Set the fully-qualified class name of the plugin.
         *
         * @param pluginClass the plugin class name
         * @return this builder instance
         */
        public Builder pluginClass(String pluginClass) {
            info.setPluginClass(pluginClass);
            return this;
        }

        /**
         * Set the list of dependencies required by the plugin.
         *
         * @param dependencies the plugin dependencies
         * @return this builder instance
         */
        public Builder dependencies(List<String> dependencies) {
            info.setDependencies(dependencies);
            return this;
        }

        /**
         * Set the type of the plugin.
         *
         * @param type the plugin type
         * @return this builder instance
         */
        public Builder type(String type) {
            info.setType(type);
            return this;
        }

        /**
         * Set the API prefix for the plugin.
         *
         * @param apiPrefix the API prefix
         * @return this builder instance
         */
        public Builder apiPrefix(String apiPrefix) {
            info.setApiPrefix(apiPrefix);
            return this;
        }

        /**
         * Set the number of REST endpoints exposed by the plugin.
         *
         * @param endpointCount the number of endpoints
         * @return this builder instance
         */
        public Builder endpointCount(Integer endpointCount) {
            info.setEndpointCount(endpointCount);
            return this;
        }

        /**
         * Set the file path of the plugin.
         *
         * @param filePath the file path
         * @return this builder instance
         */
        public Builder filePath(String filePath) {
            info.setFilePath(filePath);
            return this;
        }

        /**
         * Set the file size of the plugin.
         *
         * @param fileSize the file size in bytes
         * @return this builder instance
         */
        public Builder fileSize(Long fileSize) {
            info.setFileSize(fileSize);
            return this;
        }

        /**
         * Set the enabled state of the plugin.
         *
         * @param enabled true if the plugin is enabled, false otherwise
         * @return this builder instance
         */
        public Builder enabled(Boolean enabled) {
            info.setEnabled(enabled);
            return this;
        }

        /**
         * Set the homepage URL of the plugin.
         *
         * @param homepage the homepage URL
         * @return this builder instance
         */
        public Builder homepage(String homepage) {
            info.setHomepage(homepage);
            return this;
        }

        /**
         * Set the license information for the plugin.
         *
         * @param license the license information
         * @return this builder instance
         */
        public Builder license(String license) {
            info.setLicense(license);
            return this;
        }

        /**
         * Set the list of tags associated with the plugin.
         *
         * @param tags the list of tags
         * @return this builder instance
         */
        public Builder tags(List<String> tags) {
            info.setTags(tags);
            return this;
        }

        /**
         * Build and return the configured {@link PluginInfo} instance.
         *
         * @return the configured PluginInfo instance
         */
        public PluginInfo build() {
            return info;
        }
    }
}
