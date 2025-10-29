package cloud.xcan.angus.plugin.store;

import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiskPluginStore implements PluginStore {
    private static final Logger log = LoggerFactory.getLogger(DiskPluginStore.class);
    private final Path dir;

    public DiskPluginStore(PluginProperties properties) {
        this.dir = Paths.get(properties.getDirectory());
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            log.error("Failed to create plugin directory: {}", dir, e);
        }
    }

    @Override
    public List<String> listPluginIds() throws IOException {
        if (!Files.exists(dir)) return new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".jar"))
                    .map(p -> p.getFileName().toString())
                    .map(name -> name.replaceAll("\\.jar$", ""))
                    .collect(Collectors.toList());
        }
    }

    @Override
    @SuppressWarnings("RedundantThrows")
    public Path getPluginPath(String pluginId) throws IOException {
        Path p = dir.resolve(pluginId + ".jar");
        return Files.exists(p) ? p : null;
    }

    @Override
    public Path storePlugin(String pluginId, byte[] data) throws IOException {
        Path p = dir.resolve(pluginId + ".jar");
        Files.write(p, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return p;
    }

    @Override
    public boolean deletePlugin(String pluginId) throws IOException {
        Path p = dir.resolve(pluginId + ".jar");
        if (Files.exists(p)) {
            Files.delete(p);
            return true;
        }
        return false;
    }
}
