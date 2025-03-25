package cloud.xcan.angus.core.jpa.repository.summary;

public enum DateRangeType {
  HOUR, DAY, WEEK, MONTH, YEAR;

  public String toFormat() {
    switch (this) {
      case HOUR:
        return "%Y-%m-%d %H";
      case DAY:
        return "%Y-%m-%d";
      case WEEK:
        return "%Y-%u";
      case MONTH:
        return "%Y-%m";
      case YEAR:
        return "%Y";
      default:
        return null;
    }
  }

}
