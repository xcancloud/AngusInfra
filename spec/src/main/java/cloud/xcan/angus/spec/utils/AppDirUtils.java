package cloud.xcan.angus.spec.utils;

import static cloud.xcan.angus.spec.experimental.BizConstant.AppDir.PLUGINS_DIR_NAME;
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
    String dir = ObjectUtils.isEmpty(System.getProperty(AppDir.HOME_DIR))
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
    // 指定Springboot项目依赖lib目录
    // 方式1：
    // java -cp ./lib/* com.example.demo.DemoApplication
    // 在上述命令中，-cp ./lib/*指定了类路径，其中./lib/*表示所有依赖项（包括JAR文件和类文件）都在./lib目录中，并使用通配符*来表示所有文件
    // 方式2：
    // java -Dloader.path=./lib/ -jar demo.jar
    // 用-Dloader.path参数来指定lib目录的路径，使用-Dloader.path参数指定lib目录的路径可以让应用程序在运行时动态添加类路径，以加载额外的类或资源
    // 参考：https://blog.csdn.net/Zhai_ZB/article/details/122013533
    return ObjectUtils.isEmpty(homeDir) ? System
        .getProperty("loader.path") /* Fix:: + File.separator*/
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
    return ObjectUtils.isEmpty(homeDir) ? System
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
