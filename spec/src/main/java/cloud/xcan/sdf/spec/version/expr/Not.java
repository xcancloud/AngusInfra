package cloud.xcan.sdf.spec.version.expr;

import cloud.xcan.sdf.spec.version.Version;

/**
 * Expression for the logical "negation" operator.
 */
public class Not implements Expression {

  /**
   * The expression to negate.
   */
  private final Expression expr;

  /**
   * Constructs a {@code Not} expression with an expression to negate.
   *
   * @param expr the expression to negate
   */
  public Not(Expression expr) {
    this.expr = expr;
  }

  /**
   * Negates the given expression.
   *
   * @param version the version to interpret against
   * @return {@code true} if the given expression evaluates to {@code false} and {@code false}
   * otherwise
   */
  @Override
  public boolean interpret(Version version) {
    return !expr.interpret(version);
  }
}
