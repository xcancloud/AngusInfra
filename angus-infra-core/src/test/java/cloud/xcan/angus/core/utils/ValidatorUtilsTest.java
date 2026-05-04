package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidatorUtilsTest {

  @Test
  void testUrlExp() {
    assertTrue(ValidatorUtils.isUrl("http://www.xcan.com"));
    assertTrue(ValidatorUtils.isUrl("http://www.xcan.cloud/index.html"));
    assertTrue(ValidatorUtils.isUrl("http://www.xcan.cloud/index.html?source=library"));
    assertTrue(ValidatorUtils.isUrl("https://www.xcan.cloud"));
  }

  @Test
  void testEmailChineseIdCard() {
    assertTrue(ValidatorUtils.isEmail("a@b.co"));
    assertFalse(ValidatorUtils.isEmail("not-an-email"));

    assertTrue(ValidatorUtils.isChinese("中文"));
    assertFalse(ValidatorUtils.isChinese("abc"));

    assertTrue(ValidatorUtils.isChinaIdCard("11010119900307789x"));
    assertFalse(ValidatorUtils.isChinaIdCard("123"));
  }

  @Test
  void testDomain() {
    assertTrue(ValidatorUtils.isDomain("www.xcan.com"));
    assertFalse(ValidatorUtils.isDomain("https://www.xcan.com/"));
    assertFalse(ValidatorUtils.isDomain("http://www.xcan.com/"));
    assertFalse(ValidatorUtils.isDomain("http://127.0.0.1:8011"));
    assertTrue(ValidatorUtils.isDomain("xcan.com"));
    assertFalse(ValidatorUtils.isDomain("127.0.0.1"));
    assertFalse(ValidatorUtils.isDomain("127.0.0.1:8011"));
  }
}
