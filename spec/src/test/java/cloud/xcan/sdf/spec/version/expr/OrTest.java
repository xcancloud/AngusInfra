package cloud.xcan.sdf.spec.version.expr;

import static org.junit.Assert.assertTrue;

import cloud.xcan.sdf.spec.version.Version;
import org.junit.Test;

public class OrTest {

  @Test
  public void shouldCheckIfOneOfTwoExpressionsEvaluateToTrue() {
    Expression left = new Expression() {
      @Override
      public boolean interpret(Version version) {
        return false;
      }
    };
    Expression right = new Expression() {
      @Override
      public boolean interpret(Version version) {
        return true;
      }
    };
    Or or = new Or(left, right);
    assertTrue(or.interpret(null));
  }
}
