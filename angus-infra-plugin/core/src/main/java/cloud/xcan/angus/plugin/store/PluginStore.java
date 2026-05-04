package cloud.xcan.angus.plugin.store;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface PluginStore {

  // list available plugin identifiers
  List<String> listPluginIds() throws IOException;

  // get plugin file path or null if not present
  Path getPluginPath(String pluginId) throws IOException;

  // write plugin bytes to store and return stored path
  Path storePlugin(String pluginId, byte[] data) throws IOException;

  // delete stored plugin
  boolean deletePlugin(String pluginId) throws IOException;
}

