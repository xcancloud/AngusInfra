package cloud.xcan.sdf.spec;

import cloud.xcan.sdf.spec.setting.JavaSystemSetting;
import cloud.xcan.sdf.spec.utils.StringUtils;

public class Platform {

  private Platform() {
  }

  /**
   * Determine whether the current operation system seems to be Windows.
   */
  public static boolean isWindows() {
    return JavaSystemSetting.OS_NAME.getStringValue()
        .map(s -> StringUtils.lowerCase(s).startsWith("windows"))
        .orElse(false);
  }
}