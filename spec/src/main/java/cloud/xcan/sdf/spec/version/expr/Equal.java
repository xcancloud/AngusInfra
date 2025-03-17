package cloud.xcan.sdf.spec.version.expr;

import cloud.xcan.sdf.spec.version.Version;

/**
 * Expression for the comparison "equal" operator.
 */
public class Equal implements Expression {

  /**
   * The parsed version, the right-hand operand of the "equal" operator.
   */
  private final Version parsedVersion;

  /**
   * Constructs a {@code Equal} expression with the parsed version.
   *
   * @param parsedVersion the parsed version
   */
  public Equal(Version parsedVersion) {
    this.parsedVersion = parsedVersion;
  }

  /**
   * Checks if the current version equals the parsed version.
   *
   * @param version the version to compare to, the left-hand operand of the "equal" operator
   * @return {@code true} if the version equals the parsed version or {@code false} otherwise
   */
  @Override
  public boolean interpret(Version version) {
    return version.equals(parsedVersion);
  }
}
