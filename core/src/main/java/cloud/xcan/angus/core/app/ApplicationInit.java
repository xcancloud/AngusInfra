package cloud.xcan.angus.core.app;

public interface ApplicationInit extends Comparable<ApplicationInit> {

  int MIN_ORDER = Integer.MAX_VALUE;

  void init() throws Exception;

  int getOrder();

  @Override
  default int compareTo(ApplicationInit o) {
    return o.getOrder() - this.getOrder();
  }
}
