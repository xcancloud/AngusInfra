package cloud.xcan.angus.spec.utils;

import java.io.File;

/**
 * @author XiaoLong Liu
 */
public class AppNamingUtils {

  public static boolean isLicenseFile(String filename, boolean ignoreHidden) {
    if (ignoreHidden(filename, ignoreHidden)) {
      return false;
    }
    if (StringUtils.isBlank(filename)) {
      return false;
    }
    return filename.endsWith(".lic") || "licence".equalsIgnoreCase(filename);
  }

  public static boolean isPluginFile(String filename, boolean ignoreHidden) {
    if (ignoreHidden(filename, ignoreHidden)) {
      return false;
    }
    if (StringUtils.isBlank(filename)) {
      return false;
    }
    return filename.endsWith(".zip") && filename.contains("plugin-") && !filename
        .contains("plugin-web-");
  }

  public static boolean isWebPluginFile(String filename, boolean ignoreHidden) {
    if (ignoreHidden(filename, ignoreHidden)) {
      return false;
    }
    if (StringUtils.isBlank(filename)) {
      return false;
    }
    return filename.endsWith(".zip") && filename.contains("plugin-web-");
  }

  public static boolean isProductionPropertiesFile(String filename, boolean ignoreHidden) {
    if (ignoreHidden(filename, ignoreHidden)) {
      return false;
    }
    if (StringUtils.isBlank(filename)) {
      return false;
    }
    return "production.properties".equalsIgnoreCase(filename);
  }

  private static boolean ignoreHidden(String filename, boolean ignoreHidden) {
    if (ignoreHidden) {
      File file = new File(filename);
      return file.isHidden();
    }
    return false;
  }
}
