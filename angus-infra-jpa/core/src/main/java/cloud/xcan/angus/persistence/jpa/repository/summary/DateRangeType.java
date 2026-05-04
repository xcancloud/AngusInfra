package cloud.xcan.angus.persistence.jpa.repository.summary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

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

  public static List<String> getDateStrBetween(String startDateStr, String endDateStr,
      DateRangeType dateUnit) throws ParseException {
    SimpleDateFormat sdf;
    Calendar end;
    ArrayList<String> result = new ArrayList<>();
    Calendar start = Calendar.getInstance();
    end = Calendar.getInstance();
    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    start.setTime(sdf.parse(startDateStr));
    end.setTime(sdf.parse(endDateStr));
    label50:
    switch (dateUnit) {
      case YEAR:
        sdf = new SimpleDateFormat("yyyy");

        while (true) {
          if (!start.before(end)) {
            break label50;
          }

          result.add(sdf.format(start.getTime()));
          start.add(Calendar.YEAR, 1);
        }
      case MONTH:
        sdf = new SimpleDateFormat("yyyy-MM");

        while (true) {
          if (!start.before(end)) {
            break label50;
          }

          result.add(sdf.format(start.getTime()));
          start.add(Calendar.MONTH, 1);
        }
      case WEEK:
        sdf = new SimpleDateFormat("yyyy-w");

        while (true) {
          if (!start.before(end)) {
            break label50;
          }

          result.add(sdf.format(start.getTime()));
          start.add(Calendar.WEEK_OF_MONTH, 1);
        }
      case DAY:
        sdf = new SimpleDateFormat("yyyy-MM-dd");

        while (true) {
          if (!start.before(end)) {
            break label50;
          }

          result.add(sdf.format(start.getTime()));
          start.add(Calendar.DATE, 1);
        }
      case HOUR:
        sdf = new SimpleDateFormat("yyyy-MM-dd HH");

        while (start.before(end)) {
          result.add(sdf.format(start.getTime()));
          start.add(Calendar.HOUR_OF_DAY, 1);
        }
    }

    result.add(sdf.format(end.getTime()));
    return result.stream().distinct().collect(Collectors.toList());
  }
}
