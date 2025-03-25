package cloud.xcan.angus.spec.http;

import java.util.Comparator;
import java.util.Map;

/**
 * Strategy interface for {@code String}-based path matching.
 * <p>
 * The default implementation is {@link AntPathMatcher}, supporting the Ant-style pattern syntax.
 */
public interface PathMatcher {

  boolean isPattern(String path);

  boolean match(String pattern, String path);

  boolean matchStart(String pattern, String path);

  String extractPathWithinPattern(String pattern, String path);

  Map<String, String> extractUriTemplateVariables(String pattern, String path);

  Comparator<String> getPatternComparator(String path);

  String combine(String pattern1, String pattern2);
}
