package cloud.xcan.angus.validator;

import static cloud.xcan.angus.validator.impl.DomainValidator.REGEX_DOMAIN;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class DomainParamTest {

  private final String domain;
  private final boolean expected;

  public DomainParamTest(String domain, boolean expected) {
    this.domain = domain;
    this.expected = expected;
  }

  @Parameters(name = "{index}: isValid({0})={1}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {"www.google.com", true},
            {"google.com", true},
            {"mkyong123.com", true},
            {"mkyong-info.com", true},
            {"sub.mkyong.com", true},
            {"sub.mkyong-info.com", true},
            {"mkyong.com.au", true},
            {"sub.mkyong.com", true},
            {"sub.sub.mkyong.com", true},
            {"g.co", true},
            {"mkyong.t.t.co", true},
            {"mkyong.t.t.c", false},      //Tld must at between 2 and 6 long
            {"mkyong,com", false},  //comma not allowed
            {"mkyong", false},    //no tld
            {"mkyong.123", false},  //digit not allowed in tld
            {".com", false},    //must start with [SpecIgnoreTest-Za-z0-9]
            {"mkyong.a", false},    //last tld need at least two characters
            {"mkyong.com/users", false},  // no tld
            {"-mkyong.com", false},  //Cannot begin with a hyphen -
            {"mkyong-.com", false},  //Cannot end with a hyphen -
            {"sub.-mkyong.com", false},  //Cannot begin with a hyphen -
            {"sub.mkyong-.com", false}  //Cannot end with a hyphen -
        }
    );
  }

  @Test
  public void testDomains() {
    assertEquals(expected, Pattern.matches(REGEX_DOMAIN, domain));
  }

}
