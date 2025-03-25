package cloud.xcan.angus.spec;

import cloud.xcan.angus.spec.setting.JavaSystemSetting;
import cloud.xcan.angus.spec.utils.StringUtils;

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
