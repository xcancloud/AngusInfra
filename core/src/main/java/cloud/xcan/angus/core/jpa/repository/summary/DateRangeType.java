package cloud.xcan.angus.core.jpa.repository.summary;

public enum DateRangeType {
  HOUR, DAY, WEEK, MONTH, YEAR;

  public String toFormat() {
    return switch (this) {
      case HOUR -> "%Y-%m-%d %H";
      case DAY -> "%Y-%m-%d";
      case WEEK -> "%Y-%u";
      case MONTH -> "%Y-%m";
      case YEAR -> "%Y";
    };
  }

}
