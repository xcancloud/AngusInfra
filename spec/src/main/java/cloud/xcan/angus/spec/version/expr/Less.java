package cloud.xcan.angus.spec.version.expr;

import cloud.xcan.angus.spec.version.Version;

/**
 * Expression for the comparison "less than" operator.
 */
public class Less implements Expression {

  /**
   * The parsed version, the right-hand operand of the "less than" operator.
   */
  private final Version parsedVersion;

  /**
   * Constructs a {@code Less} expression with the parsed version.
   *
   * @param parsedVersion the parsed version
   */
  Less(Version parsedVersion) {
    this.parsedVersion = parsedVersion;
  }

  /**
   * Checks if the current version is less than the parsed version.
   *
   * @param version the version to compare to, the left-hand operand of the "less than" operator
   * @return {@code true} if the version is less than the parsed version or {@code false} otherwise
   */
  @Override
  public boolean interpret(Version version) {
    return version.lessThan(parsedVersion);
  }
}
