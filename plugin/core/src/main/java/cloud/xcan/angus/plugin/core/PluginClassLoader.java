package cloud.xcan.angus.plugin.core;

import java.io.Closeable;
import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader implements Closeable {

  public PluginClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  @Override
  public void close() {
    try {
      super.close();
    } catch (Exception ignored) {
    }
  }
}

