package cloud.xcan.angus.spec.version.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cloud.xcan.angus.spec.version.Version;
import org.junit.Test;

public class LessTest {

  @Test
  public void shouldCheckIfVersionIsLessThanParsedVersion() {
    Version parsed = Version.valueOf("2.0.0");
    Less lt = new Less(parsed);
    assertTrue(lt.interpret(Version.valueOf("1.2.3")));
    assertFalse(lt.interpret(Version.valueOf("3.2.1")));
  }
}
