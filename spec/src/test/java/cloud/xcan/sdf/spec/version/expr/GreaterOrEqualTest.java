package cloud.xcan.angus.spec.version.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cloud.xcan.angus.spec.version.Version;
import org.junit.Test;

public class GreaterOrEqualTest {

  @Test
  public void shouldCheckIfVersionIsGreaterThanOrEqualToParsedVersion() {
    Version parsed = Version.valueOf("2.0.0");
    GreaterOrEqual ge = new GreaterOrEqual(parsed);
    assertTrue(ge.interpret(Version.valueOf("3.2.1")));
    assertTrue(ge.interpret(Version.valueOf("2.0.0")));
    assertFalse(ge.interpret(Version.valueOf("1.2.3")));
  }
}
