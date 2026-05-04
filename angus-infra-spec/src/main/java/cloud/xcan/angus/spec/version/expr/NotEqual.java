package cloud.xcan.angus.spec.version.expr;

import cloud.xcan.angus.spec.version.Version;

/**
 * Expression for the comparison "not equal" operator.
 */
public class NotEqual implements Expression {

  /**
   * The parsed version, the right-hand operand of the "not equal" operator.
   */
  private final Version parsedVersion;

  /**
   * Constructs a {@code NotEqual} expression with the parsed version.
   *
   * @param parsedVersion the parsed version
   */
  public NotEqual(Version parsedVersion) {
    this.parsedVersion = parsedVersion;
  }

  /**
   * Checks if the current version does not equal the parsed version.
   *
   * @param version the version to compare with, the left-hand operand of the "not equal" operator
   * @return {@code true} if the version does not equal the parsed version or {@code false}
   * otherwise
   */
  @Override
  public boolean interpret(Version version) {
    return !version.equals(parsedVersion);
  }
}
