package cloud.xcan.angus.spec.utils;

import static cloud.xcan.angus.spec.experimental.BizConstant.AppDir.LICENSE_DIR_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.AppDir.PLUGINS_DIR_NAME;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import cloud.xcan.angus.spec.experimental.BizConstant.AppDir;
import java.io.File;

/**
 * When not specified, the default root path is the user's home directory
 */
public class AppDirUtils {

  public String getHomeDir() {
    String dir = isEmpty(System.getProperty(AppDir.HOME_DIR))
        ? System.getProperty("user.dir") : System.getProperty(AppDir.HOME_DIR);
    return dir.endsWith(File.separator) ? dir : dir + File.separator;
  }

  public String getLibDir() {
    return getLibDir(getHomeDir());
  }

  public String getLibDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.LIB_DIR), System.getenv(AppDir.LIB_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = AppDir.LIB_DIR_NAME + File.separator;
    // Specify the lib directory for Spring Boot project dependencies
    // Method 1:
    // java -cp ./lib/* com.example.demo.DemoApplication
    // In the above command, -cp ./lib/* specifies the classpath, where ./lib/* indicates that all dependencies (including JAR files and class files) are in the ./lib directory, using the wildcard * to represent all files.
    // Method 2:
    // java -Dloader.path=./lib/ -jar demo.jar
    // Use the -Dloader.path parameter to specify the path to the lib directory; using the -Dloader.path parameter allows the application to dynamically add the classpath at runtime to load additional classes or resources.
    // Reference: https://blog.csdn.net/Zhai_ZB/article/details/122013533
    return isEmpty(homeDir) ? System.getProperty("loader.path") /* Fix:: + File.separator*/
        : homeDir + path;
  }

  public String getTmpDir() {
    return getTmpDir(getHomeDir());
  }

  public String getTmpDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.TMP_DIR), System.getenv(AppDir.TMP_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = AppDir.TMP_DIR_NAME + File.separator;
    return isEmpty(homeDir) ? System
        .getProperty("java.io.tmpdir") /* Fix:: + File.separator*/
        : homeDir + path;
  }

  public String getBizTmpDir(String bizDir) {
    return getTmpDir(getHomeDir()) + bizDir + File.separator;
  }

  public String getBizWorkDir(String homeDir, String bizDir) {
    return getTmpDir(homeDir) + bizDir + File.separator;
  }

  public String getWorkDir() {
    return getWorkDir(getHomeDir());
  }

  public String getWorkDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.WORK_DIR), System.getenv(AppDir.WORK_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = AppDir.WORK_DIR_NAME + File.separator;
    return isNotEmpty(homeDir) ? homeDir + path : path;
  }

  public String getBizWorkDir(String bizDir) {
    return getWorkDir(getHomeDir()) + bizDir + File.separator;
  }

  public String getWorkDir(String homeDir, String bizDir) {
    return getWorkDir(homeDir) + bizDir + File.separator;
  }

  public String getDataDir() {
    return getDataDir(getHomeDir());
  }

  public String getDataDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.DATA_DIR), System.getenv(AppDir.DATA_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = AppDir.DATA_DIR_NAME + File.separator;
    return isNotEmpty(homeDir) ? homeDir + path : path;
  }

  public String getBizDataDir(String bizDir) {
    return getDataDir(getHomeDir()) + bizDir + File.separator;
  }

  public String getBizDataDir(String homeDir, String bizDir) {
    return getDataDir(homeDir) + bizDir + File.separator;
  }

  public String getLogsDir() {
    return getLogsDir(getHomeDir());
  }

  public String getLogsDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.LOGS_DIR), System.getenv(AppDir.LOGS_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = AppDir.LOGS_DIR_NAME + File.separator;
    return isNotEmpty(homeDir) ? homeDir + path : path;
  }

  public String getBizLogsDir(String bizDir) {
    return getLogsDir(getHomeDir()) + bizDir + File.separator;
  }

  public String getBizLogsDir(String homeDir, String bizDir) {
    return getDataDir(homeDir) + bizDir + File.separator;
  }

  public String getConfDir() {
    return getConfDir(getHomeDir());
  }

  public String getConfDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.CONFIG_DIR), System.getenv(AppDir.CONFIG_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = AppDir.CONFIG_DIR_NAME + File.separator;
    return isNotEmpty(homeDir) ? homeDir + path : path;
  }

  public String getPluginDir() {
    return getPluginDir(getHomeDir());
  }

  public String getPluginDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.PLUGINS_DIR),
        System.getenv(AppDir.PLUGINS_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = PLUGINS_DIR_NAME + File.separator;
    return isNotEmpty(homeDir) ? homeDir + path : path;
  }

  public String getSubAppDir() {
    return getPluginDir(getHomeDir());
  }

  public String getSubAppDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.SUB_APP_DIR),
        System.getenv(AppDir.SUB_APP_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = AppDir.SUB_APP_DIR_NAME + File.separator;
    return isNotEmpty(homeDir) ? homeDir + path : path;
  }

  public String getSubAppDirPlugin(String subAppName) {
    return getSubAppDir() + subAppName + File.separator + PLUGINS_DIR_NAME;
  }

  public String getSubAppDirPlugin(String homeDir, String subAppName) {
    return getSubAppDir(homeDir) + subAppName + File.separator + PLUGINS_DIR_NAME;
  }

  public String getStaticDir() {
    return getStaticDir(getHomeDir());
  }

  public String getBizStaticDir(String bizDir) {
    return getStaticDir(getHomeDir()) + bizDir;
  }

  public String getStaticDir(String homeDir) {
    String dir = nullSafe(System.getProperty(AppDir.STATICS_DIR),
        System.getenv(AppDir.STATICS_DIR));
    if (isNotBlank(dir)) {
      return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }
    String path = AppDir.STATICS_DIR_NAME + File.separator;
    return isNotEmpty(homeDir) ? homeDir + path : path;
  }

  public String getStaticPluginDir() {
    return getStaticPluginDir(getHomeDir());
  }

  public String getStaticPluginDir(String homeDir) {
    return getStaticDir(homeDir) + PLUGINS_DIR_NAME;
  }

  public String getBizConfDir(String bizDir) {
    return getConfDir(getHomeDir()) + bizDir + File.separator;
  }

  public String getBizConfDir(String homeDir, String bizDir) {
    return getConfDir(homeDir) + bizDir + File.separator;
  }

  public String getLicenceDir() {
    return getLicenceDir(getHomeDir());
  }

  public String getLicenceDir(String homeDir) {
    return getStaticDir(homeDir) + LICENSE_DIR_NAME + File.separator;
  }

  public static long getUsedDiskSpace() {
    final File rootFilePath = new File(".");
    long totalDiskSpace = rootFilePath.getTotalSpace();
    return totalDiskSpace - rootFilePath.getFreeSpace();
  }

  public static long getTotalDiskSpace() {
    final File rootFilePath = new File(".");
    return rootFilePath.getTotalSpace();
  }
}
