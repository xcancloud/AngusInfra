package cloud.xcan.angus.spec.version.expr;

import static org.junit.Assert.assertTrue;

import cloud.xcan.angus.spec.version.Version;
import org.junit.Test;

public class AndTest {

  @Test
  public void shouldCheckIfBothExpressionsEvaluateToTrue() {
    Expression left = new Expression() {
      @Override
      public boolean interpret(Version version) {
        return true;
      }
    };
    Expression right = new Expression() {
      @Override
      public boolean interpret(Version version) {
        return true;
      }
    };
    And and = new And(left, right);
    assertTrue(and.interpret(null));
  }
}
