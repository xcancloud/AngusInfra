package cloud.xcan.angus.spec.version.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cloud.xcan.angus.spec.version.Version;
import org.junit.Test;

public class NotEqualTest {

  @Test
  public void shouldCheckIfVersionIsNotEqualToParsedVersion() {
    Version parsed = Version.valueOf("1.2.3");
    NotEqual ne = new NotEqual(parsed);
    assertTrue(ne.interpret(Version.valueOf("3.2.1")));
    assertFalse(ne.interpret(Version.valueOf("1.2.3")));
  }
}
