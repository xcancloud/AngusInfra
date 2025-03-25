package cloud.xcan.angus.spec.version.expr;

import cloud.xcan.angus.spec.version.Version;

/**
 * Expression for the logical "or" operator.
 */
public class Or implements Expression {

  /**
   * The left-hand operand of expression.
   */
  private final Expression left;

  /**
   * The right-hand operand of expression.
   */
  private final Expression right;

  /**
   * Constructs a {@code Or} expression with the left-hand and right-hand operands.
   *
   * @param left  the left-hand operand of expression
   * @param right the right-hand operand of expression
   */
  public Or(Expression left, Expression right) {
    this.left = left;
    this.right = right;
  }

  /**
   * Checks if one of the operands evaluates to {@code true}.
   *
   * @param version the version to interpret against
   * @return {@code true} if one of the operands evaluates to {@code true} or {@code false}
   * otherwise
   */
  @Override
  public boolean interpret(Version version) {
    return left.interpret(version) || right.interpret(version);
  }
}
