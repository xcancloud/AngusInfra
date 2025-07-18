package cloud.xcan.angus.validator;

import static cloud.xcan.angus.validator.impl.CodeValidator.REGEX_CODE;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class CodeParamTest {

  private final String code;
  private final boolean expected;

  public CodeParamTest(String code, boolean expected) {
    this.code = code;
    this.expected = expected;
  }

  @Parameters(name = "{index}: isValid({0})={1}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {"abc", true},
            {"ABV", true},
            {"abc112c", true},
            {"Anc8B", true},
            {"a-Anc1-22", true},
            {"Bvs_28jhN_", true},
            {"aaJ:00:1991", true},
            {"aa.ss90V-ss:00c", true},
            {"aA1-_:.", true},
            {" ssssV", false},
            {"@#sksk", false},
            {"--+", false},
            {"海", false},
            {" ", false},
            {"]", false},
        }
    );
  }

  @Test
  public void testCodes() {
    assertEquals(expected, Pattern.matches(REGEX_CODE, code));
  }

}
