package cloud.xcan.sdf.spec.version;

import cloud.xcan.sdf.spec.utils.StringUtils;
import cloud.xcan.sdf.spec.version.expr.Expression;

/**
 * Default implementation for {@link VersionManager}. This implementation uses jSemVer (a Java
 * implementation of the SemVer Specification).
 */
public class DefaultVersionManager implements VersionManager {

  /**
   * Checks if a version satisfies the specified SemVer {@link Expression} string. If the constraint
   * is empty or null then the method returns true. Constraint examples: {@code >2.0.0} (simple),
   * {@code ">=1.4.0 & <1.6.0"} (range). See https://github.com/zafarkhaja/jsemver#semver-expressions-api-ranges
   * for more info.
   */
  @Override
  public boolean checkVersionConstraint(String version, String constraint) {
    return StringUtils.isNullOrEmpty(constraint)
        || Version.valueOf(/*Fix::Ignore -SNAPSHOT*/Version.valueOf(version).getNormalVersion())
        .satisfies(constraint);
  }

  @Override
  public int compareVersions(String v1, String v2) {
    return Version.valueOf(v1).compareTo(Version.valueOf(v2));
  }

}
