package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.spec.experimental.BizConstant.AppDir;
import org.junit.jupiter.api.Test;

class SpringAppDirUtilsTest {

  @Test
  void getHomeDir_notBlank_defaultingToApplicationHome() {
    SpringAppDirUtils utils = new SpringAppDirUtils();
    String home = utils.getHomeDir();
    assertNotNull(home);
    assertFalse(home.isBlank());
  }

  @Test
  void getHomeDir_respectsSystemProperty() {
    String previous = System.getProperty(AppDir.HOME_DIR);
    String marker = "xcan-home-test-" + System.nanoTime();
    String custom = System.getProperty("java.io.tmpdir") + marker;
    try {
      System.setProperty(AppDir.HOME_DIR, custom);
      SpringAppDirUtils utils = new SpringAppDirUtils();
      String home = utils.getHomeDir();
      assertTrue(home.contains(marker), () -> "home=" + home);
    } finally {
      if (previous == null) {
        System.clearProperty(AppDir.HOME_DIR);
      } else {
        System.setProperty(AppDir.HOME_DIR, previous);
      }
    }
  }
}
