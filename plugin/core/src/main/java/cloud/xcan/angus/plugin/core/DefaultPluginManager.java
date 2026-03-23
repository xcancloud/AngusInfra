package cloud.xcan.angus.plugin.core;

import cloud.xcan.angus.plugin.api.Plugin;
import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.api.PluginManager;
import cloud.xcan.angus.plugin.api.RestfulPlugin;
import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import cloud.xcan.angus.plugin.event.PluginEvent;
import cloud.xcan.angus.plugin.event.PluginFailedEvent;
import cloud.xcan.angus.plugin.event.PluginLoadedEvent;
import cloud.xcan.angus.plugin.event.PluginStartedEvent;
import cloud.xcan.angus.plugin.event.PluginStoppedEvent;
import cloud.xcan.angus.plugin.event.PluginUnloadedEvent;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginDescriptor;
import cloud.xcan.angus.plugin.model.PluginInfo;
import cloud.xcan.angus.plugin.model.PluginState;
import cloud.xcan.angus.plugin.store.PluginStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class DefaultPluginManager implements PluginManager {

  private static final Logger log = LoggerFactory.getLogger(DefaultPluginManager.class);

  private final ApplicationContext applicationContext;
  private final PluginProperties properties;
  private final DynamicRestEndpointManager restEndpointManager;

  // Optional plugin store (disk or jpa)
  private PluginStore pluginStore;

  private final Map<String, PluginWrapper> plugins = new ConcurrentHashMap<>();
  private final Map<String, PluginClassLoader> classLoaders = new ConcurrentHashMap<>();

  public DefaultPluginManager(ApplicationContext applicationContext,
      PluginProperties properties,
      DynamicRestEndpointManager restEndpointManager) {
    this.applicationContext = applicationContext;
    this.properties = properties;
    this.restEndpointManager = restEndpointManager;
  }

  // Allow auto-config to inject chosen PluginStore implementation
  public void setPluginStore(PluginStore pluginStore) {
    this.pluginStore = pluginStore;
  }

  @Override
  public void initialize() {
    log.info("Initializing Plugin Manager...");
    createDirectories();
    if (properties.isAutoLoad()) {
      loadAllPlugins();
    }
  }

  private void createDirectories() {
    try {
      Path pluginDir = Paths.get(properties.getDirectory());
      if (!Files.exists(pluginDir)) {
        Files.createDirectories(pluginDir);
      }
      Path dataDir = Paths.get(properties.getDataDirectory());
      if (!Files.exists(dataDir)) {
        Files.createDirectories(dataDir);
      }
    } catch (IOException e) {
      log.error("Failed to create directories", e);
    }
  }

  @Override
  public void loadAllPlugins() {
    // If a PluginStore is configured, prefer listing from it (disk or jpa)
    if (pluginStore != null) {
      try {
        List<String> ids = pluginStore.listPluginIds();
        for (String id : ids) {
          try {
            Path p = pluginStore.getPluginPath(id);
            if (p != null) {
              loadPlugin(p);
            } else {
              log.warn("Plugin {} listed in store but path not available", id);
            }
          } catch (Exception e) {
            log.error("Failed to load plugin from store: {}", id, e);
          }
        }
      } catch (IOException e) {
        log.error("Failed to list plugins from store", e);
      }
      return;
    }

    Path pluginDir = Paths.get(properties.getDirectory());
    if (!Files.exists(pluginDir)) {
      log.warn("Plugin directory not found: {}", pluginDir);
      return;
    }
    try (Stream<Path> stream = Files.list(pluginDir)) {
      List<Path> pluginFiles = stream.filter(path -> path.toString().endsWith(".jar")).toList();
      for (Path pluginFile : pluginFiles) {
        try {
          loadPlugin(pluginFile);
        } catch (Exception e) {
          log.error("Failed to load plugin: {}", pluginFile, e);
        }
      }
    } catch (IOException e) {
      log.error("Failed to scan plugin directory", e);
    }
  }

  @Override
  public boolean loadPlugin(Path pluginPath) {
    PluginDescriptor descriptor = null;
    try {
      if (pluginPath == null) {
        log.warn("Plugin path is null, cannot load");
        return false;
      }
      descriptor = readPluginDescriptor(pluginPath);
      if (descriptor == null) {
        return false;
      }
      if (!validatePlugin(descriptor)) {
        return false;
      }
      if (plugins.containsKey(descriptor.getId())) {
        log.warn("Plugin {} already loaded", descriptor.getId());
        return false;
      }
      PluginClassLoader classLoader = createPluginClassLoader(pluginPath, descriptor);
      classLoaders.put(descriptor.getId(), classLoader);
      Class<?> pluginClass = classLoader.loadClass(descriptor.getPluginClass());
      if (!Plugin.class.isAssignableFrom(pluginClass)) {
        log.error("Plugin class must implement Plugin interface: {}", descriptor.getPluginClass());
        return false;
      }
      Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();
      PluginWrapper wrapper = new PluginWrapper();
      wrapper.setPlugin(plugin);
      wrapper.setDescriptor(descriptor);
      wrapper.setClassLoader(classLoader);
      wrapper.setPluginPath(pluginPath);
      wrapper.setState(PluginState.INITIALIZED);
      wrapper.setLoadedAt(LocalDateTime.now());
      plugins.put(descriptor.getId(), wrapper);
      PluginContext context = createPluginContext(wrapper);
      wrapper.setContext(context);
      plugin.initialize(context);

      // Register dynamic REST endpoints if plugin exposes REST controllers
      if (plugin instanceof RestfulPlugin) {
        try {
          boolean registered = restEndpointManager.registerPlugin(descriptor.getId(),
              (RestfulPlugin) plugin, context);
          if (!registered) {
            log.warn("REST endpoint registration returned false for plugin: {}",
                descriptor.getId());
          }
        } catch (Exception e) {
          log.error("Failed to register REST endpoints for plugin: {}", descriptor.getId(), e);
        }
      }

      // After plugin initialization, auto-start and publish events
      log.info("Plugin {} (v{}) loaded and started", descriptor.getId(), descriptor.getVersion());
      publishEvent(new PluginLoadedEvent(this, descriptor.getId(), descriptor.getName(),
          descriptor.getVersion()));
      return true;
    } catch (PluginException e) {
      String pluginId =
          descriptor != null ? descriptor.getId() : pluginPath.getFileName().toString();
      String pluginName = descriptor != null ? descriptor.getName() : pluginId;
      String pluginVersion = descriptor != null ? descriptor.getVersion() : "";
      publishEvent(
          new PluginFailedEvent(this, pluginId, pluginName, pluginVersion, "start failed", e));
      return false;
    } catch (Exception e) {
      log.error("Failed to load plugin from: {}", pluginPath, e);
      return false;
    }
  }

  /**
   * Install plugin bytes into the configured store (disk or jpa) and load it.
   */
  @Override
  public boolean installPlugin(String pluginId, byte[] data) throws PluginException {
    validatePluginId(pluginId);
    try {
      Path stored;
      if (pluginStore != null) {
        stored = pluginStore.storePlugin(pluginId, data);
      } else {
        Path dir = Paths.get(properties.getDirectory());
        if (!Files.exists(dir)) {
          Files.createDirectories(dir);
        }
        stored = dir.resolve(pluginId + ".jar");
        Files.write(stored, data);
      }
      return loadPlugin(stored);
    } catch (Exception e) {
      log.error("Failed to install plugin {}", pluginId, e);
      throw new PluginException("install.failed", e);
    }
  }

  /**
   * Remove plugin from manager and optionally from store.
   */
  @Override
  public boolean removePlugin(String pluginId, boolean removeFromStore) throws PluginException {
    validatePluginId(pluginId);
    boolean unloaded = unloadPlugin(pluginId);
    if (!unloaded) {
      return false;
    }
    try {
      if (pluginStore != null && removeFromStore) {
        return pluginStore.deletePlugin(pluginId);
      } else if (removeFromStore) {
        Path p = Paths.get(properties.getDirectory()).resolve(pluginId + ".jar");
        if (Files.exists(p)) {
          Files.delete(p);
          return true;
        }
        return false;
      }
    } catch (Exception e) {
      log.error("Failed to remove plugin from store: {}", pluginId, e);
      throw new PluginException("remove.failed", e);
    }
    return true;
  }

  // Simplified start/stop implementations omitted for brevity; unloadPlugin is implemented to properly close class loaders

  private PluginDescriptor readPluginDescriptor(Path pluginPath) {
    try (JarFile jarFile = new JarFile(pluginPath.toFile())) {
      JarEntry entry = jarFile.getJarEntry("plugin.json");
      if (entry == null) {
        entry = jarFile.getJarEntry("META-INF/plugin.json");
      }
      if (entry != null) {
        byte[] bytes = jarFile.getInputStream(entry).readAllBytes();
        return new ObjectMapper().readValue(bytes, PluginDescriptor.class);
      }
    } catch (Exception e) {
      log.error("Failed to read plugin descriptor from: {}", pluginPath, e);
    }
    return null;
  }

  private static void validatePluginId(String pluginId) throws PluginException {
    if (pluginId == null || !pluginId.matches("[a-zA-Z0-9._\\-]+")) {
      throw new PluginException("Invalid pluginId format: " + pluginId);
    }
  }

  private boolean validatePlugin(PluginDescriptor descriptor) {
    if (descriptor.getId() == null || descriptor.getId().trim().isEmpty()) {
      log.error("Plugin ID is required");
      return false;
    }
    if (descriptor.getPluginClass() == null || descriptor.getPluginClass().trim().isEmpty()) {
      log.error("Plugin class is required");
      return false;
    }

    // P1-8: Validate dependencies if present
    if (descriptor.getDependencies() != null && !descriptor.getDependencies().isEmpty()) {
      for (String depId : descriptor.getDependencies()) {
        if (!hasPlugin(depId)) {
          log.warn("Plugin {} dependency not found: {}", descriptor.getId(), depId);
        }
      }
    }

    // P1-8: Check minimum system version
    if (descriptor.getMinSystemVersion() != null && !descriptor.getMinSystemVersion().isEmpty()) {
      String systemVersion = properties.getDefaultConfiguration()
          .getOrDefault("system.version", "1.0.0").toString();
      if (isVersionLess(systemVersion, descriptor.getMinSystemVersion())) {
        log.warn("Plugin {} requires system version {}, current is {}", descriptor.getId(),
            descriptor.getMinSystemVersion(), systemVersion);
      }
    }

    // P1-8: Warn about required permissions
    if (descriptor.getRequiredPermissions() != null && !descriptor.getRequiredPermissions()
        .isEmpty()) {
      if (properties.isEnableSecurityCheck()) {
        log.info("Plugin {} requires permissions: {}", descriptor.getId(),
            String.join(", ", descriptor.getRequiredPermissions()));
      }
    }

    if (properties.isValidateOnStartup()) {
      log.debug("Validation passed for plugin: {}", descriptor.getId());
    }
    return true;
  }

  private boolean isVersionLess(String actual, String required) {
    String[] actualParts = actual.split("\\.");
    String[] requiredParts = required.split("\\.");
    for (int i = 0; i < Math.max(actualParts.length, requiredParts.length); i++) {
      int actualNum =
          i < actualParts.length ? Integer.parseInt(actualParts[i].replaceAll("\\D", "0")) : 0;
      int requiredNum =
          i < requiredParts.length ? Integer.parseInt(requiredParts[i].replaceAll("\\D", "0")) : 0;
      if (actualNum < requiredNum) {
        return true;
      } else if (actualNum > requiredNum) {
        return false;
      }
    }
    return false;
  }

  private PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor descriptor)
      throws IOException {
    List<java.net.URL> urls = new ArrayList<>();
    urls.add(pluginPath.toUri().toURL());
    if (descriptor.getLibraries() != null) {
      Path libDir = pluginPath.getParent().resolve(descriptor.getId() + "-lib");
      if (Files.exists(libDir)) {
        try (Stream<Path> stream = Files.list(libDir)) {
          stream.filter(path -> path.toString().endsWith(".jar")).forEach(path -> {
            try {
              urls.add(path.toUri().toURL());
            } catch (Exception e) {
              log.error("Failed to add library: {}", path, e);
            }
          });
        }
      }
    }
    return new PluginClassLoader(urls.toArray(new java.net.URL[0]), getClass().getClassLoader());
  }

  private PluginContext createPluginContext(PluginWrapper wrapper) {
    Map<String, Object> config = new HashMap<>(properties.getDefaultConfiguration());
    if (wrapper.getDescriptor().getConfiguration() != null) {
      config.putAll(wrapper.getDescriptor().getConfiguration());
    }
    String pluginId = wrapper.getDescriptor().getId();
    if (properties.getPluginConfigurations().containsKey(pluginId)) {
      config.putAll(properties.getPluginConfigurations().get(pluginId));
    }
    return new DefaultPluginContext(applicationContext, wrapper.getDescriptor(), config,
        properties);
  }

  @Override
  public boolean startPlugin(String pluginId) throws PluginException {
    PluginWrapper wrapper = plugins.get(pluginId);
    if (wrapper == null) {
      log.warn("Plugin not found for start: {}", pluginId);
      return false;
    }
    if (wrapper.getState() == PluginState.STARTED) {
      log.debug("Plugin {} already started", pluginId);
      return true;
    }
    try {
      wrapper.getPlugin().start();
      wrapper.setState(PluginState.STARTED);
      wrapper.setStartedAt(LocalDateTime.now());
      log.info("Plugin {} started", pluginId);
      publishEvent(new PluginStartedEvent(this, pluginId, wrapper.getDescriptor().getName(),
          wrapper.getDescriptor().getVersion()));
      return true;
    } catch (PluginException e) {
      wrapper.setState(PluginState.ERROR);
      log.error("Failed to start plugin {}: {}", pluginId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public boolean stopPlugin(String pluginId) throws PluginException {
    PluginWrapper wrapper = plugins.get(pluginId);
    if (wrapper == null) {
      log.warn("Plugin not found for stop: {}", pluginId);
      return false;
    }
    if (wrapper.getState() == PluginState.STOPPED) {
      return true;
    }
    try {
      wrapper.getPlugin().stop();
      wrapper.setState(PluginState.STOPPED);
      log.info("Plugin {} stopped", pluginId);
      publishEvent(new PluginStoppedEvent(this, pluginId, wrapper.getDescriptor().getName(),
          wrapper.getDescriptor().getVersion()));
      return true;
    } catch (PluginException e) {
      wrapper.setState(PluginState.ERROR);
      log.error("Failed to stop plugin {}: {}", pluginId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public boolean unloadPlugin(String pluginId) {
    PluginWrapper wrapper = plugins.remove(pluginId);
    if (wrapper == null) {
      log.warn("Plugin not found: {}", pluginId);
      return false;
    }
    try {
      wrapper.setState(PluginState.UNLOADING);
      Plugin plugin = wrapper.getPlugin();
      if (plugin != null) {
        try {
          plugin.stop();
        } catch (PluginException e) {
          log.warn("Error stopping plugin {}: {}", pluginId, e.getMessage());
        }
        try {
          plugin.destroy();
        } catch (PluginException e) {
          log.warn("Error destroying plugin {}: {}", pluginId, e.getMessage());
        }
      }

      // Unregister dynamic REST endpoints if applicable
      try {
        restEndpointManager.unregisterPlugin(pluginId);
      } catch (Exception e) {
        log.warn("Error unregistering REST endpoints for plugin {}: {}", pluginId, e.getMessage());
      }

      // Close and remove classloader, then clean up any temp jar extracted by JpaPluginStore
      PluginClassLoader classLoader = classLoaders.remove(pluginId);
      if (classLoader != null) {
        try {
          classLoader.close();
        } catch (Exception ignored) {
        }
        Path pluginPath = wrapper.getPluginPath();
        if (pluginPath != null) {
          try {
            // Only remove jars created by JpaPluginStore (Files.createTempFile); do not delete
            // plugins living under e.g. junit @TempDir/.../plugins — those paths also sit under
            // java.io.tmpdir and would break reload/remove flows.
            String name = pluginPath.getFileName().toString();
            boolean jpaSpillFile =
                (name.startsWith("plugin-") || name.startsWith("plugin-store-")) && name.endsWith(".jar");
            Path tmpDir = Path.of(System.getProperty("java.io.tmpdir", ""));
            if (jpaSpillFile && !tmpDir.toString().isEmpty()
                && pluginPath.normalize().startsWith(tmpDir.normalize())) {
              Files.deleteIfExists(pluginPath);
            }
          } catch (Exception e) {
            log.debug("Could not delete temp plugin file {}: {}", pluginPath, e.getMessage());
          }
        }
      }

      log.info("Plugin {} unloaded", pluginId);
      publishEvent(new PluginUnloadedEvent(this, pluginId, wrapper.getDescriptor().getName(),
          wrapper.getDescriptor().getVersion()));
      return true;
    } catch (Exception e) {
      log.error("Failed to unload plugin {}", pluginId, e);
      return false;
    }
  }

  @Override
  public boolean reloadPlugin(String pluginId) {
    PluginWrapper wrapper = plugins.get(pluginId);
    if (wrapper == null) {
      log.warn("Plugin not found: {}", pluginId);
      return false;
    }
    Path path = wrapper.getPluginPath();
    if (path == null) {
      log.warn("Plugin path not available for: {}", pluginId);
      return false;
    }
    if (!unloadPlugin(pluginId)) {
      return false;
    }
    return loadPlugin(path);
  }

  @Override
  public List<PluginInfo> getAllPlugins() {
    List<PluginInfo> out = new ArrayList<>();
    for (PluginWrapper w : plugins.values()) {
      PluginDescriptor d = w.getDescriptor();
      out.add(PluginInfo.builder()
          .id(d.getId())
          .name(d.getName())
          .version(d.getVersion())
          .description(d.getDescription())
          .author(d.getAuthor())
          .state(w.getState())
          .loadedAt(w.getLoadedAt())
          .startedAt(w.getStartedAt())
          .pluginClass(d.getPluginClass())
          .dependencies(d.getDependencies())
          .apiPrefix(d.getName() != null ? "/api/plugins/" + d.getId() : null)
          .endpointCount(restEndpointManager.getPluginEndpoints(d.getId()).size())
          .filePath(w.getPluginPath() != null ? w.getPluginPath().toString() : null)
          .fileSize(w.getPluginPath() != null ? getFileSize(w.getPluginPath()) : null)
          .build());
    }
    return out;
  }

  private Long getFileSize(Path path) {
    try {
      return Files.size(path);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public PluginInfo getPluginInfo(String pluginId) {
    PluginWrapper w = plugins.get(pluginId);
    if (w == null) {
      return null;
    }
    PluginDescriptor d = w.getDescriptor();
    return PluginInfo.builder()
        .id(d.getId())
        .name(d.getName())
        .version(d.getVersion())
        .description(d.getDescription())
        .author(d.getAuthor())
        .state(w.getState())
        .loadedAt(w.getLoadedAt())
        .startedAt(w.getStartedAt())
        .pluginClass(d.getPluginClass())
        .dependencies(d.getDependencies())
        .apiPrefix(d.getName() != null ? "/api/plugins/" + d.getId() : null)
        .endpointCount(restEndpointManager.getPluginEndpoints(d.getId()).size())
        .filePath(w.getPluginPath() != null ? w.getPluginPath().toString() : null)
        .fileSize(w.getPluginPath() != null ? getFileSize(w.getPluginPath()) : null)
        .build();
  }

  @Override
  public boolean hasPlugin(String pluginId) {
    return plugins.containsKey(pluginId);
  }

  private void publishEvent(PluginEvent event) {
    try {
      applicationContext.publishEvent(event);
    } catch (Exception e) {
      log.debug("Failed to publish plugin event: {}", event.getClass().getSimpleName(), e);
    }
  }
}
