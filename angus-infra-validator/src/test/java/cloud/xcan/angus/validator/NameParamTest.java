package cloud.xcan.angus.validator;

import static cloud.xcan.angus.validator.impl.NameValidator.REGEX_NAME;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class NameParamTest {

  private final String name;
  private final boolean expected;

  public NameParamTest(String name, boolean expected) {
    this.name = name;
    this.expected = expected;
  }

  @Parameters(name = "{index}: isValid({0})={1}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {"abc", true},
            {"ABV", true},
            {"abc112c", true},
            {"Anc8B./", true},
            {"a-Anc1-22", true},
            {"Bvs_28jhN_", true},
            {"aaJ:s", false},
            {"aa\"sss\"0c", false},
            {"a  A", false}
        }
    );
  }

  @Test
  public void testTests() {
    assertEquals(expected, Pattern.matches(REGEX_NAME, name));
  }

}
