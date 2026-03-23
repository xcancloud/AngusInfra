package cloud.xcan.angus.core.app;

public interface ApplicationInit extends Comparable<ApplicationInit> {

  int MIN_ORDER = Integer.MAX_VALUE;

  void init() throws Exception;

  int getOrder();

  /**
   * Higher {@link #getOrder()} runs earlier (e.g. {@code 0} before {@code -100}).
   */
  @Override
  default int compareTo(ApplicationInit o) {
    return Integer.compare(o.getOrder(), this.getOrder());
  }
}
