package cloud.xcan.angus.plugin.support;

import cloud.xcan.angus.plugin.model.PluginDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class PluginJarBuilder {

  private PluginJarBuilder() {
  }

  public static void build(Path jarFile, PluginDescriptor descriptor,
      List<Class<?>> embeddedClasses) throws IOException {
    byte[] json = new ObjectMapper().writeValueAsBytes(descriptor);
    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFile))) {
      putEntry(jos, "plugin.json", json);
      for (Class<?> c : embeddedClasses) {
        String resource = c.getSimpleName() + ".class";
        String pathInJar = c.getName().replace('.', '/') + ".class";
        byte[] classBytes;
        try {
          classBytes = Files.readAllBytes(Path.of(c.getResource(resource).toURI()));
        } catch (Exception e) {
          throw new IOException("Cannot read class bytes for " + c.getName(), e);
        }
        putEntry(jos, pathInJar, classBytes);
      }
    }
  }

  public static void buildWithMetaInfPluginJson(Path jarFile, PluginDescriptor descriptor,
      List<Class<?>> embeddedClasses) throws IOException {
    byte[] json = new ObjectMapper().writeValueAsBytes(descriptor);
    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFile))) {
      putEntry(jos, "META-INF/plugin.json", json);
      for (Class<?> c : embeddedClasses) {
        String resource = c.getSimpleName() + ".class";
        String pathInJar = c.getName().replace('.', '/') + ".class";
        byte[] classBytes;
        try {
          classBytes = Files.readAllBytes(Path.of(c.getResource(resource).toURI()));
        } catch (Exception e) {
          throw new IOException("Cannot read class bytes for " + c.getName(), e);
        }
        putEntry(jos, pathInJar, classBytes);
      }
    }
  }

  private static void putEntry(JarOutputStream jos, String name, byte[] data)
      throws IOException {
    JarEntry entry = new JarEntry(name);
    jos.putNextEntry(entry);
    jos.write(data);
    jos.closeEntry();
  }
}
