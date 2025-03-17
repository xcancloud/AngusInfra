package cloud.xcan.sdf.spec.setting;

import static cloud.xcan.sdf.spec.setting.AppSettingHelper.getSetting;

import org.junit.Assert;
import org.junit.Test;

public class AppSettingHelperTest {

  @Test
  public void testLoadingProperty() {
    AppSettingHelper.Setting SETTING = getSetting("default.properties", "custom.properties",
        AppSettingHelperTest.class);

    Assert.assertEquals("10", SETTING.getString("a")); // loading from custom configuration.
    Assert.assertEquals("20", SETTING.getString("b")); // loading from default configuration.

    System.setProperty("c", "40");
    System.setProperty("b", "40");

    Assert.assertEquals("40", SETTING.getString("c")); // loading from system property.
    Assert.assertEquals("20", SETTING.getString("b")); // loading from cache.

  }

}
