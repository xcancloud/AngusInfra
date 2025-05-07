package cloud.xcan.angus.core.spring.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Setter
@Getter
@ConfigurationProperties(prefix = "xcan.global", ignoreInvalidFields = true)
public class GlobalProperties {

  private Cors cors = new Cors();
  private final String[] defaultStartsWithPaths = {"/actuator"};

  @Getter
  @Setter
  public static class Cors {

    private Boolean enabled = true;
    /**
     * All paths take effect when not set.
     */
    private String[] startWithPaths;
    private String credentials = "true";
    private String origin = "*";
    private String headers = "*";
    private String methods = "POST, GET, PUT, DELETE, PATCH, HEAD, OPTIONS";
    private String exposeHeaders = "*";

  }

  public boolean isDefault(String uri) {
    for (String defaultStartsWithPath : defaultStartsWithPaths) {
      if (uri.startsWith(defaultStartsWithPath)) {
        return true;
      }
    }
    return false;
  }

  public boolean allowedPaths(String uri) {
    if (!cors.getEnabled()) {
      return false;
    }
    if (cors.startWithPaths == null || cors.startWithPaths.length == 0) {
      return true;
    }
    for (String startsWithPath : cors.startWithPaths) {
      if ("/".equals(startsWithPath) || uri.startsWith(startsWithPath)) {
        return true;
      }
    }
    return false;
  }
}
