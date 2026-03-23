package cloud.xcan.angus.spec.runner;

import cloud.xcan.angus.spec.utils.file.filter.JarFileFilter;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * Reference: org.hyperic.sigar.cmd.Runner
 */
public final class ClassInvoker {

  public static final String JAVA_CLASS_PATH = "java.class.path";
  private static final String JAR_DIRECTORY;

  static {
    JAR_DIRECTORY = resolveJarDirectory();

    StringBuilder classpath = new StringBuilder(256);
    List<URL> jars = buildUpdatedClassPath(JAR_DIRECTORY, classpath);
    System.setProperty(JAVA_CLASS_PATH, classpath.toString());

    URL[] urls = jars.toArray(URL[]::new);
    URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
    Thread.currentThread().setContextClassLoader(loader);
  }

  private static String resolveJarDirectory() {
    try {
      ProtectionDomain pd = ClassInvoker.class.getProtectionDomain();
      CodeSource cs = pd == null ? null : pd.getCodeSource();
      URL location = cs == null ? null : cs.getLocation();
      if (location != null) {
        File self = new File(decodePath(location.getFile()));
        String parent = self.getParent();
        if (parent != null) {
          return parent;
        }
        return self.getAbsolutePath();
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    return System.getProperty("user.dir", ".");
  }

  private static String decodePath(String path) {
    if (path == null || path.isEmpty()) {
      return path == null ? "" : path;
    }
    return URLDecoder.decode(path, StandardCharsets.UTF_8);
  }

  private static List<URL> buildUpdatedClassPath(String jarDir, StringBuilder classpath) {
    List<URL> jars = new ArrayList<>();
    List<File> libDirs = new ArrayList<>(2);
    File base = new File(jarDir).getAbsoluteFile();
    libDirs.add(base);

    File ext = new File(jarDir, "ext");
    if (ext.isDirectory()) {
      libDirs.add(ext.getAbsoluteFile());
    }

    for (File libDir : libDirs) {
      File[] libJars = libDir.listFiles(new JarFileFilter());
      if (libJars == null) {
        continue;
      }
      addFiles(libJars, jars, classpath);
    }
    return jars;
  }

  private static void addFiles(File[] libJars, List<URL> jars, StringBuilder classpath) {
    for (File libJar : libJars) {
      try {
        jars.add(libJar.toURI().toURL());
        if (classpath.length() > 0) {
          classpath.append(File.pathSeparatorChar);
        }
        classpath.append(libJar.getPath());
      } catch (MalformedURLException e) {
        e.printStackTrace(System.err);
      }
    }
  }

  /**
   * Get the directory where this module's jar (or classes directory) resides; absolute path.
   */
  public static String getJarLocation() {
    return JAR_DIRECTORY;
  }

  /**
   * Reflectively loads a class, constructs it with the no-arg constructor, and invokes a method.
   *
   * @param clazz                fully qualified class name
   * @param methodName           method name
   * @param args                 method arguments (may be {@code null} if the method has no
   *                             parameters)
   * @param methodParameterTypes parameter types of the method
   * @return the method return value
   * @throws Throwable the error from loading, construction, or invocation (for
   *                   {@link InvocationTargetException}, the target exception is rethrown)
   */
  public static Object invoke(String clazz, String methodName,
      Object[] args, Class<?>... methodParameterTypes) throws Throwable {
    try {
      Class<?> initialClass =
          Thread.currentThread().getContextClassLoader().loadClass(clazz);
      Object instance = initialClass.getDeclaredConstructor().newInstance();
      Method startup = initialClass.getMethod(methodName, methodParameterTypes);
      return startup.invoke(instance, args);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        System.err.println("ERROR: " + cause);
        System.err.println("Home directory was detected as: " + JAR_DIRECTORY);
        throw cause;
      }
      System.err.println("Home directory was detected as: " + JAR_DIRECTORY);
      throw e;
    } catch (Throwable e) {
      if (e.getCause() != null) {
        System.err.println("ERROR: " + e.getCause());
        System.err.println("Home directory was detected as: " + JAR_DIRECTORY);
        throw e.getCause();
      }
      System.err.println("Home directory was detected as: " + JAR_DIRECTORY);
      throw e;
    }
  }
}
