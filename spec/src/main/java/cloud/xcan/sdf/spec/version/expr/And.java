package cloud.xcan.sdf.spec.version.expr;

import cloud.xcan.sdf.spec.version.Version;

/**
 * Expression for the logical "and" operator.
 */
public class And implements Expression {

  /**
   * The left-hand operand of expression.
   */
  private final Expression left;

  /**
   * The right-hand operand of expression.
   */
  private final Expression right;

  /**
   * Constructs a {@code And} expression with the left-hand and right-hand operands.
   *
   * @param left  the left-hand operand of expression
   * @param right the right-hand operand of expression
   */
  public And(Expression left, Expression right) {
    this.left = left;
    this.right = right;
  }

  /**
   * Checks if both operands evaluate to {@code true}.
   *
   * @param version the version to interpret against
   * @return {@code true} if both operands evaluate to {@code true} or {@code false} otherwise
   */
  @Override
  public boolean interpret(Version version) {
    return left.interpret(version) && right.interpret(version);
  }
}
