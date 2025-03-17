package cloud.xcan.sdf.spec.version.expr;

import static cloud.xcan.sdf.spec.version.expr.CompositeExpression.Helper.eq;
import static cloud.xcan.sdf.spec.version.expr.CompositeExpression.Helper.gt;
import static cloud.xcan.sdf.spec.version.expr.CompositeExpression.Helper.gte;
import static cloud.xcan.sdf.spec.version.expr.CompositeExpression.Helper.lt;
import static cloud.xcan.sdf.spec.version.expr.CompositeExpression.Helper.lte;
import static cloud.xcan.sdf.spec.version.expr.CompositeExpression.Helper.neq;
import static cloud.xcan.sdf.spec.version.expr.CompositeExpression.Helper.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CompositeExpressionTest {

  @Test
  public void shouldSupportEqualExpression() {
    assertTrue(eq("1.0.0").interpret("1.0.0"));
    assertFalse(eq("1.0.0").interpret("2.0.0"));
  }

  @Test
  public void shouldSupportNotEqualExpression() {
    assertTrue(neq("1.0.0").interpret("2.0.0"));
  }

  @Test
  public void shouldSupportGreaterExpression() {
    assertTrue(gt("1.0.0").interpret("2.0.0"));
    assertFalse(gt("2.0.0").interpret("1.0.0"));
  }

  @Test
  public void shouldSupportGreaterOrEqualExpression() {
    assertTrue(gte("1.0.0").interpret("1.0.0"));
    assertTrue(gte("1.0.0").interpret("2.0.0"));
    assertFalse(gte("2.0.0").interpret("1.0.0"));
  }

  @Test
  public void shouldSupportLessExpression() {
    assertTrue(lt("2.0.0").interpret("1.0.0"));
    assertFalse(lt("1.0.0").interpret("2.0.0"));
  }

  @Test
  public void shouldSupportLessOrEqualExpression() {
    assertTrue(lte("1.0.0").interpret("1.0.0"));
    assertTrue(lte("2.0.0").interpret("1.0.0"));
    assertFalse(lte("1.0.0").interpret("2.0.0"));
  }

  @Test
  public void shouldSupportNotExpression() {
    assertTrue(not(eq("1.0.0")).interpret("2.0.0"));
    assertFalse(not(eq("1.0.0")).interpret("1.0.0"));
  }

  @Test
  public void shouldSupportAndExpression() {
    assertTrue(gt("1.0.0").and(lt("2.0.0")).interpret("1.5.0"));
    assertFalse(gt("1.0.0").and(lt("2.0.0")).interpret("2.5.0"));
  }

  @Test
  public void shouldSupportOrExpression() {
    assertTrue(lt("1.0.0").or(gt("1.0.0")).interpret("1.5.0"));
    assertFalse(gt("1.0.0").or(gt("2.0.0")).interpret("0.5.0"));
  }

  @Test
  public void shouldSupportComplexExpressions() {
    /* ((>=1.0.1 & <2) | (>=3.0 & <4)) & ((1-1.5) & (~1.5)) */
    CompositeExpression expr =
        gte("1.0.1").and(
            lt("2.0.0").or(
                gte("3.0.0").and(
                    lt("4.0.0").and(
                        gte("1.0.0").and(
                            lte("1.5.0").and(
                                gte("1.5.0").and(
                                    lt("2.0.0")
                                )
                            )
                        )
                    )
                )
            )
        );
    assertTrue(expr.interpret("1.5.0"));
    assertFalse(expr.interpret("2.5.0"));
  }
}
