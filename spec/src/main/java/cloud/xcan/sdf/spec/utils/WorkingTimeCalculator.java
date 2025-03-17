package cloud.xcan.sdf.spec.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class WorkingTimeCalculator {

  public static BigDecimal calcWorkingHours(LocalDateTime start, LocalDateTime end) {
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("The end time must be after the start time");
    }

    LocalTime startWorkTime = LocalTime.of(9, 0); // [9 ~ 12) = 3 hours
    LocalTime endWorkTime = LocalTime.of(17, 0); // [12 ~ 17) = 5 hours
    double totalMinutes = 0d;
    LocalDateTime current = start;
    while (current.isBefore(end) || current.isEqual(end)) {
      if (isWorkingDay(current.toLocalDate())) {
        LocalDateTime workStartTime = current.toLocalDate().atTime(startWorkTime);
        LocalDateTime workEndTime = current.toLocalDate().atTime(endWorkTime);
        LocalDateTime effectiveStart = current.isBefore(workStartTime) ? workStartTime : current;
        LocalDateTime effectiveEnd = end.isBefore(workEndTime) ? end : workEndTime;
        if (effectiveStart.isBefore(effectiveEnd)) {
          totalMinutes += Duration.between(effectiveStart, effectiveEnd).toMinutes();
        }
      }
      current = current.plusDays(1).toLocalDate().atTime(startWorkTime);
    }
    BigDecimal bd = new BigDecimal(totalMinutes / 60);
    bd = bd.setScale(1, RoundingMode.HALF_UP);
    return bd;
  }

  public static long calcWorkingDays(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();

    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    long workingDays = 0;

    for (long i = 0; i < totalDays; i++) {
      LocalDate date = startDate.plusDays(i);
      if (isWorkingDay(date)) {
        workingDays++;
      }
    }

    return workingDays;
  }

  public static boolean isWorkingDay(LocalDate date) {
    DayOfWeek day = date.getDayOfWeek();
    return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
  }

  public static boolean isToday(LocalDateTime dateTime) {
    LocalDateTime now = LocalDateTime.now();
    return dateTime.toLocalDate().isEqual(now.toLocalDate());
  }

  public static boolean isLast24Hour(LocalDateTime dateTime) {
    LocalDateTime now = LocalDateTime.now();
    return dateTime.isAfter(now.minusHours(24));
  }

  public static boolean isLastWeek(LocalDateTime dateTime) {
    LocalDateTime now = LocalDateTime.now();
    return dateTime.isAfter(now.minusDays(7));
  }

  public static boolean isLastMonth(LocalDateTime dateTime) {
    LocalDateTime now = LocalDateTime.now();
    return dateTime.isAfter(now.minusMonths(1));
  }

}
