package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import org.junit.Test;

public class ValidatorUtilsTest {

  @Test
  public void testMobileExp() {
    assertTrue(Pattern.matches(MobileExp.CN.getExp(), "+86-18310363631"));
    assertTrue(Pattern.matches(MobileExp.JP.getExp(), "+819012345678"));
    assertTrue(Pattern.matches(MobileExp.JP.getExp(), "042-632-8510"));
  }

  @Test
  public void testUrlExp() {
    assertTrue(ValidatorUtils.isUrl("http://www.xcan.com"));
    assertTrue(ValidatorUtils.isUrl("http://www.xcan.cloud/index.html"));
    assertTrue(ValidatorUtils.isUrl("http://www.xcan.cloud/index.html?source=library"));
    assertTrue(ValidatorUtils.isUrl("https://www.xcan.cloud"));
  }

  @Test
  public void testMobile() {
    assertTrue(ValidatorUtils.isMobile("18910691729"));
    assertTrue(ValidatorUtils.isMobile("8618910691729"));
    assertTrue(ValidatorUtils.isMobile("08618910691729"));
    assertTrue(ValidatorUtils.isMobile("5417543010"));
    assertTrue(ValidatorUtils.isMobile("15417543010"));
    assertTrue(ValidatorUtils.isMobile("0015417543010"));

    assertTrue(Pattern.matches(MobileExp.CN.getExp(), "+86-18310363631"));
  }

  @Test
  public void testITU_TE_123_Mobile() {
    assertTrue(ValidatorUtils.isItute123Mobile("+18910691729"));
    assertTrue(ValidatorUtils.isItute123Mobile("+8618910691729"));
    assertTrue(ValidatorUtils.isItute123Mobile("+08618910691729"));
    assertTrue(ValidatorUtils.isItute123Mobile("+5417543010"));
    assertTrue(ValidatorUtils.isItute123Mobile("+15417543010"));
    assertTrue(ValidatorUtils.isItute123Mobile("+0015417543010"));
  }

  @Test
  public void testChinaMobile() {
    assertTrue(ValidatorUtils.isChinaMobile("18910691729"));
    assertTrue(ValidatorUtils.isChinaMobile("18610691729"));
    assertTrue(!ValidatorUtils.isChinaMobile("28610691729"));
    assertTrue(!ValidatorUtils.isChinaMobile("186106917290"));
  }

  @Test
  public void testITC_CN_Mobile() {
    assertTrue(ValidatorUtils.isMobile("CN", "18910691729"));
    assertTrue(ValidatorUtils.isMobile("CN", "18610691729"));
    assertTrue(ValidatorUtils.isMobile("CN", "8618910691729"));
    assertTrue(ValidatorUtils.isMobile("CN", "08618910691729"));
    assertTrue(!ValidatorUtils.isMobile("CN", "08628910691729"));
    assertTrue(!ValidatorUtils.isMobile("CN", "086189106917290"));
  }

  /**
   * <pre>
   * 754-3010 Local
   * (541) 754-3010 Domestic
   * +1-541-754-3010 International
   * 1-541-754-3010 Dialed in the US
   * 001-541-754-3010 Dialed from Germany
   * 191 541 754 3010 Dialed from France
   * </pre>
   */
  @Test
  public void testITC_US_Mobile() {
    assertTrue(ValidatorUtils.isMobile("US", "5417543010"));
    assertTrue(ValidatorUtils.isMobile("US", "15417543010"));
    assertTrue(!ValidatorUtils.isMobile("US", "0015417543010"));
  }
}
