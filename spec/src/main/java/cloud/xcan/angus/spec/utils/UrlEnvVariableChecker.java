package cloud.xcan.angus.spec.utils;

import java.util.regex.Pattern;

public final class UrlEnvVariableChecker {

  private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\{([^}]+)\\}");

  private UrlEnvVariableChecker() {
  }

  public static boolean containsEnvVariable(String url) {
    if (url == null || url.isEmpty()) {
      return false;
    }
    return ENV_VAR_PATTERN.matcher(url).find();
  }
}
