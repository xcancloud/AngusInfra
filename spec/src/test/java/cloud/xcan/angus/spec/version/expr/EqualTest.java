package cloud.xcan.angus.spec.version.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cloud.xcan.angus.spec.version.Version;
import org.junit.Test;

public class EqualTest {

  @Test
  public void shouldCheckIfVersionIsEqualToParsedVersion() {
    Version parsed = Version.valueOf("1.2.3");
    Equal eq = new Equal(parsed);
    assertTrue(eq.interpret(Version.valueOf("1.2.3")));
    assertFalse(eq.interpret(Version.valueOf("3.2.1")));
  }
}
