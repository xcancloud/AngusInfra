package cloud.xcan.sdf.spec.version.expr;

import cloud.xcan.sdf.spec.version.Version;

/**
 * The {@code Expression} interface is to be implemented by the nodes of the Abstract Syntax Tree
 * produced by the {@code ExpressionParser} class.
 */
public interface Expression {

  /**
   * Interprets the expression.
   *
   * @param version the version to interpret against
   * @return the result of the expression interpretation
   */
  boolean interpret(Version version);
}
