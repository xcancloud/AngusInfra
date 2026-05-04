package cloud.xcan.angus.plugin.core;

import java.net.URL;
import org.junit.jupiter.api.Test;

class PluginClassLoaderTest {

  @Test
  void closeDoesNotThrow() throws Exception {
    try (PluginClassLoader cl = new PluginClassLoader(new URL[0],
        PluginClassLoaderTest.class.getClassLoader())) {
      // no-op
    }
  }
}
