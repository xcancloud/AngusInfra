package cloud.xcan.sdf.spec.version.expr;

import cloud.xcan.sdf.spec.version.Version;

/**
 * Expression for the comparison "greater than or equal to" operator.
 */
public class GreaterOrEqual implements Expression {

  /**
   * The parsed version, the right-hand operand of the "greater than or equal to" operator.
   */
  private final Version parsedVersion;

  /**
   * Constructs a {@code GreaterOrEqual} expression with the parsed version.
   *
   * @param parsedVersion the parsed version
   */
  public GreaterOrEqual(Version parsedVersion) {
    this.parsedVersion = parsedVersion;
  }

  /**
   * Checks if the current version is greater than or equal to the parsed version.
   *
   * @param version the version to compare to, the left-hand operand of the "greater than or equal
   *                to" operator
   * @return {@code true} if the version is greater than or equal to the parsed version or {@code
   * false} otherwise
   */
  @Override
  public boolean interpret(Version version) {
    return version.greaterThanOrEqualTo(parsedVersion);
  }
}
