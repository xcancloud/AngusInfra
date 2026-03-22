package cloud.xcan.angus.core.jpa.repository.summary;

public enum GroupBy {
  DATE, STATUS;

  public boolean isDateRange() {
    return this.equals(DATE);
  }

  public static boolean isDateRange(String groupByType) {
    return DATE.name().equalsIgnoreCase(groupByType);
  }
}
