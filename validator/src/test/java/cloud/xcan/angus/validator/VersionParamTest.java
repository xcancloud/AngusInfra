package cloud.xcan.angus.validator;

import static cloud.xcan.angus.validator.impl.VersionValidator.VERSION_REGEX;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class VersionParamTest {

  private final String version;
  private final boolean expected;

  public VersionParamTest(String version, boolean expected) {
    this.version = version;
    this.expected = expected;
  }

  @Parameters(name = "{index}: isValid({0})={1}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {"123", false},
            {"1.0", false},
            {"1.0.1", true},
            {"v1", false},
            {"v1.0.1", false},
            {"1.0.0-SNAPSHOT", true},
            {"1.0.0-BETA", true},
            {"1.0.0-RELEASE", true},
            {"abc", false},
            {"abc-123", false}
        }
    );
  }

  @Test
  public void testVersion() {
    assertEquals(expected, Pattern.matches(VERSION_REGEX, version));
  }

}
