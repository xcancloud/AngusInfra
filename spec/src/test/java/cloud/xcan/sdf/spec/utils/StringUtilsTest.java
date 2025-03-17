package cloud.xcan.sdf.spec.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void escapeJsonWhenNullShouldReturnEmptyString() {
    assertThat(StringUtils.escapeJson(null)).isEmpty();
  }

  @Test
  void escapeJsonWhenEmptyStringShouldReturnEmptyString() {
    assertThat(StringUtils.escapeJson("")).isEmpty();
  }

  @Test
  void escapeJsonWhenDoubleQuotesShouldReturnEscapedString() {
    assertThat(StringUtils.escapeJson("\"Hello, world!\""))
        .isEqualTo("\\\"Hello, world!\\\"");
  }

  @Test
  void testCamelToUnder() {
    assertEquals("aaa_aaa_aaa", StringUtils.camelToUnder("aaaAaaAaa"));
    assertEquals("aaa_aaa_a_a_a", StringUtils.camelToUnder("aaaAaaAAA"));
  }

  @Test
  void testUnderToCamel() {
    assertEquals("aaaAaaAAA", StringUtils.underToCamel("aaa_Aaa_AAA"));
  }

  @Test
  void testUnderToUpperCamel() {
    assertEquals("AaaAaaAaa", StringUtils.underToUpperCamel("aaa_Aaa_AAA", true));
    assertEquals("AaaAaaAAA", StringUtils.underToUpperCamel("aaa_Aaa_AAA", false));
  }

  @Test
  public void camelSplitTest(){
    String camelCaseString = "myVariableName";
    List<String> splits = StringUtils.camelSplit(camelCaseString);
    assertEquals(splits, List.of("my", "variable", "name"));
  }
}
