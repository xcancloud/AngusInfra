package cloud.xcan.angus.spec.http;

public final class PathMatchers {

  private static final PathMatcher DEFAULT = new AntPathMatcher();

  public static PathMatcher getPathMatcher() {
    return DEFAULT;
  }
}
