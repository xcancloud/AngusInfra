package cloud.xcan.angus.plugin.jpa;

import cloud.xcan.angus.plugin.store.PluginStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaPluginStore implements PluginStore {

  private static final Logger log = LoggerFactory.getLogger(JpaPluginStore.class);

  private final PluginRepository repository;

  public JpaPluginStore(PluginRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<String> listPluginIds() throws IOException {
    List<String> out = new ArrayList<>();
    repository.findAll().forEach(e -> out.add(e.getId()));
    return out;
  }

  @Override
  public Path getPluginPath(String pluginId) throws IOException {
    Optional<PluginEntity> opt = repository.findById(pluginId);
    if (opt.isEmpty()) {
      return null;
    }
    PluginEntity e = opt.get();
    Path tmp = Files.createTempFile("plugin-" + pluginId + "-", ".jar");
    Files.write(tmp, e.getData());
    log.debug("Wrote plugin {} to temp path {}", pluginId, tmp);
    return tmp;
  }

  @Override
  public Path storePlugin(String pluginId, byte[] data) throws IOException {
    PluginEntity e = new PluginEntity(pluginId, pluginId, null, data, LocalDateTime.now());
    repository.save(e);
    Path tmp = Files.createTempFile("plugin-store-" + pluginId + "-", ".jar");
    Files.write(tmp, data);
    return tmp;
  }

  @Override
  public boolean deletePlugin(String pluginId) throws IOException {
    if (repository.existsById(pluginId)) {
      repository.deleteById(pluginId);
      return true;
    }
    return false;
  }
}
