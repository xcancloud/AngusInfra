package cloud.xcan.angus.spec.version.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cloud.xcan.angus.spec.version.Version;
import org.junit.Test;

public class NotTest {

  @Test
  public void shouldRevertBooleanResultOfExpression() {
    Expression expr1 = new Expression() {
      @Override
      public boolean interpret(Version version) {
        return false;
      }
    };
    Expression expr2 = new Expression() {
      @Override
      public boolean interpret(Version version) {
        return true;
      }
    };
    Not not;
    not = new Not(expr1);
    assertTrue(not.interpret(null));
    not = new Not(expr2);
    assertFalse(not.interpret(null));
  }
}
