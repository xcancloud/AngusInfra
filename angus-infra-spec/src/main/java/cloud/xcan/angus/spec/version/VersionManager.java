package cloud.xcan.angus.spec.version;

import java.util.Comparator;

/**
 * Manager responsible for versions of plugins.
 */
public interface VersionManager {

  /**
   * Check if a {@code constraint} and a {@code version} match. A possible constrain can be
   * {@code >=1.0.0 & <2.0.0}.
   */
  boolean checkVersionConstraint(String version, String constraint);

  /**
   * Compare two versions. It's similar with {@link Comparator#compare(Object, Object)}.
   */
  int compareVersions(String v1, String v2);

}
