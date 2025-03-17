package cloud.xcan.sdf.spec.runner;

import static cloud.xcan.sdf.spec.SpecConstant.DEFAULT_ENCODING;

import cloud.xcan.sdf.spec.utils.file.filter.JarFileFilter;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

/**
 * Reference: org.hyperic.sigar.cmd.Runner
 */
public final class ClassInvoker {

  public static final String JAVA_CLASS_PATH = "java.class.path";
  private static final String JAR_DIRECTORY;

  static {
    // System.setProperty("java.awt.headless", "true");
    StringBuffer classpath = new StringBuffer();

    File self = new File(
        ClassInvoker.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    JAR_DIRECTORY = decodePath(self.getParent());
    // Add standard jar locations to initial classpath
    List<URL> jars = buildUpdatedClassPath(JAR_DIRECTORY, classpath);
    String cp = classpath.toString();
    System.setProperty(JAVA_CLASS_PATH, cp);

    URL[] urls = jars.toArray(new URL[0]);
    URLClassLoader loader = new URLClassLoader(urls);
    Thread.currentThread().setContextClassLoader(loader);
  }

  private static String decodePath(String path) {
    try {
      return URLDecoder.decode(path, DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace(System.out);
      return path;
    }
  }

  private static List<URL> buildUpdatedClassPath(String jarDir, StringBuffer classpath) {
    List<URL> jars = new LinkedList<>();
    List<File> libDirs = new LinkedList<File>();
    File file = new File(jarDir);
    libDirs.add(file.getAbsoluteFile());

    // add lib subdir
    file = new File(jarDir + File.separator + "ext");
    if (file.exists()) {
      libDirs.add(file.getAbsoluteFile());
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

  private static void addFiles(File[] libJars, List<URL> jars, StringBuffer classpath) {
    for (File libJar : libJars) {
      try {
        String s = libJar.getPath();
        jars.add(new File(s).toURI().toURL());
        classpath.append(File.pathSeparator);
        classpath.append(s);
      } catch (MalformedURLException e) {
        e.printStackTrace(System.err);
      }
    }
  }

  /**
   * Get the directory where CMD jar is placed. This is the absolute path name.
   *
   * @return the directory where JMeter is installed.
   */
  public static String getJarLocation() {
    return JAR_DIRECTORY;
  }

  /**
   * The main program which actually runs Cli or Agent.
   *
   * @param clazz                invoke class
   * @param methodName           the method name of invoke class
   * @param args                 the arguments of method
   * @param methodParameterTypes the method parameter types
   * @throws Throwable class not found or invoke exception
   */
  public static Object invoke(String clazz, String methodName,
      Object[] args, Class<?>... methodParameterTypes) throws Throwable {
    try {
      Class<?> initialClass;
      // make it independent - get class name & method from props/manifest
      initialClass = Thread.currentThread().getContextClassLoader().loadClass(clazz);
      Object instance = initialClass.newInstance();
      Method startup = initialClass.getMethod(methodName, methodParameterTypes);
      return startup.invoke(instance, args);
    } catch (Throwable e) {
      if (e.getCause() != null) {
        System.err.println("ERROR: " + e.getCause().toString());
        System.err.println("Home directory was detected as: " + JAR_DIRECTORY);
        throw e.getCause();
      } else {
        System.err.println("Home directory was detected as: " + JAR_DIRECTORY);
        throw e;
      }
    }
  }

}
