package cloud.xcan.angus.spec.version.expr;

import cloud.xcan.angus.spec.version.Version;

/**
 * Expression for the comparison "greater than" operator.
 */
public class Greater implements Expression {

  /**
   * The parsed version, the right-hand operand of the "greater than" operator.
   */
  private final Version parsedVersion;

  /**
   * Constructs a {@code Greater} expression with the parsed version.
   *
   * @param parsedVersion the parsed version
   */
  public Greater(Version parsedVersion) {
    this.parsedVersion = parsedVersion;
  }

  /**
   * Checks if the current version is greater than the parsed version.
   *
   * @param version the version to compare to, the left-hand operand of the "greater than" operator
   * @return {@code true} if the version is greater than the parsed version or {@code false}
   * otherwise
   */
  @Override
  public boolean interpret(Version version) {
    return version.greaterThan(parsedVersion);
  }
}
