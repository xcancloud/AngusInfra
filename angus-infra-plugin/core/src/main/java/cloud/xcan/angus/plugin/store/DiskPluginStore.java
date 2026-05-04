package cloud.xcan.angus.plugin.store;

import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskPluginStore implements PluginStore {

  private static final Logger log = LoggerFactory.getLogger(DiskPluginStore.class);
  private final Path dir;

  public DiskPluginStore(PluginProperties properties) {
    this.dir = Paths.get(properties.getDirectory());
    try {
      if (!Files.exists(dir)) {
        Files.createDirectories(dir);
      }
    } catch (IOException e) {
      log.error("Failed to create plugin directory: {}", dir, e);
    }
  }

  @Override
  public List<String> listPluginIds() throws IOException {
    if (!Files.exists(dir)) {
      return new ArrayList<>();
    }
    try (Stream<Path> stream = Files.list(dir)) {
      return stream
          .filter(p -> p.toString().endsWith(".jar"))
          .map(p -> p.getFileName().toString())
          .map(name -> name.replaceAll("\\.jar$", ""))
          .collect(Collectors.toList());
    }
  }

  private Path resolveAndValidate(String pluginId) throws IOException {
    if (pluginId == null || !pluginId.matches("[a-zA-Z0-9._\\-]+")) {
      throw new IOException("Invalid pluginId format: " + pluginId);
    }
    Path resolved = dir.resolve(pluginId + ".jar").normalize();
    if (!resolved.startsWith(dir.normalize())) {
      throw new IOException("Path traversal detected for pluginId: " + pluginId);
    }
    return resolved;
  }

  @Override
  public Path getPluginPath(String pluginId) throws IOException {
    Path p = resolveAndValidate(pluginId);
    return Files.exists(p) ? p : null;
  }

  @Override
  public Path storePlugin(String pluginId, byte[] data) throws IOException {
    Path p = resolveAndValidate(pluginId);
    Files.write(p, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    return p;
  }

  @Override
  public boolean deletePlugin(String pluginId) throws IOException {
    Path p = resolveAndValidate(pluginId);
    if (Files.exists(p)) {
      Files.delete(p);
      return true;
    }
    return false;
  }
}
