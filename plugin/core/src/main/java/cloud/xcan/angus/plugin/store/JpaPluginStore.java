package cloud.xcan.angus.plugin.store;

/**
 * Placeholder for JPA-backed store implementation.
 * The full JPA implementation is provided by the plugin-starter module where JPA dependencies are available.
 */
public class JpaPluginStore implements PluginStore {
    @Override
    public java.util.List<String> listPluginIds() throws java.io.IOException {
        throw new UnsupportedOperationException("JpaPluginStore is provided by plugin-starter module");
    }

    @Override
    public java.nio.file.Path getPluginPath(String pluginId) throws java.io.IOException {
        throw new UnsupportedOperationException("JpaPluginStore is provided by plugin-starter module");
    }

    @Override
    public java.nio.file.Path storePlugin(String pluginId, byte[] data) throws java.io.IOException {
        throw new UnsupportedOperationException("JpaPluginStore is provided by plugin-starter module");
    }

    @Override
    public boolean deletePlugin(String pluginId) throws java.io.IOException {
        throw new UnsupportedOperationException("JpaPluginStore is provided by plugin-starter module");
    }
}
