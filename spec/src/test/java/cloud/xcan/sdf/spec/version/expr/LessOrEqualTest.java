package cloud.xcan.sdf.spec.version.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cloud.xcan.sdf.spec.version.Version;
import org.junit.Test;

public class LessOrEqualTest {

  @Test
  public void shouldCheckIfVersionIsLessThanOrEqualToParsedVersion() {
    Version parsed = Version.valueOf("2.0.0");
    LessOrEqual le = new LessOrEqual(parsed);
    assertTrue(le.interpret(Version.valueOf("1.2.3")));
    assertTrue(le.interpret(Version.valueOf("2.0.0")));
    assertFalse(le.interpret(Version.valueOf("3.2.1")));
  }
}
