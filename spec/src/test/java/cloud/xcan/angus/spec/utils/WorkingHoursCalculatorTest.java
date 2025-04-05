package cloud.xcan.angus.spec.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class WorkingHoursCalculatorTest {

  @Test
  public void testCalcWorkingHours_SimpleCase() {
    LocalDateTime start = LocalDateTime.of(2024, 10, 28, 10, 0); // 星期一
    LocalDateTime end = LocalDateTime.of(2024, 10, 31, 16, 0);   // 星期四
    long expectedHours = 30; // 3天的工作时长 (3 * 8小时 + 16 - 10)

    long actualHours = WorkingTimeCalculator.calcWorkingHours(start, end).longValue();
    assertEquals(expectedHours, actualHours);
  }

  @Test
  public void testCalcWorkingHours_WithWeekend() {
    LocalDateTime start = LocalDateTime.of(2024, 10, 25, 10, 0); // 星期五
    LocalDateTime end = LocalDateTime.of(2024, 10, 30, 16, 0);   // 星期三
    long expectedHours = 30; // 3天的工作时长 (3 * 8小时 + 16 - 10)

    long actualHours = WorkingTimeCalculator.calcWorkingHours(start, end).longValue();
    assertEquals(expectedHours, actualHours);
  }

  @Test
  public void testCalcWorkingHours_OverlappingNonWorkingHours() {
    LocalDateTime start = LocalDateTime.of(2024, 10, 28, 17, 0); // 星期一 18:00
    LocalDateTime end = LocalDateTime.of(2024, 10, 29, 9, 0);   // 星期二 9:00
    long expectedHours = 0; // 不在工作时间内

    long actualHours = WorkingTimeCalculator.calcWorkingHours(start, end).longValue();
    assertEquals(expectedHours, actualHours);
  }

  @Test
  public void testCalcWorkingHours_OnlyWeekend() {
    LocalDateTime start = LocalDateTime.of(2024, 10, 26, 17, 0); // 星期六
    LocalDateTime end = LocalDateTime.of(2024, 10, 28, 9, 0);   // 星期一
    long expectedHours = 0; // 不在工作时间内

    long actualHours = WorkingTimeCalculator.calcWorkingHours(start, end).longValue();
    assertEquals(expectedHours, actualHours);
  }

  @Test
  public void testCalcWorkingHours_WeekendAndNonWorkingHours() {
    LocalDateTime start = LocalDateTime.of(2024, 10, 26, 18, 0); // 星期六
    LocalDateTime end = LocalDateTime.of(2024, 10, 28, 23, 0);   // 星期一
    long expectedHours = 8; // 不在工作时间内

    long actualHours = WorkingTimeCalculator.calcWorkingHours(start, end).longValue();
    assertEquals(expectedHours, actualHours);
  }

  @Test
  public void testCalcWorkingHours_SingleDay() {
    LocalDateTime start = LocalDateTime.of(2024, 10, 30, 10, 0); // 星期三
    LocalDateTime end = LocalDateTime.of(2024, 10, 30, 15, 0);   // 星期三
    long expectedHours = 5; // 3小时  (15 - 10)

    long actualHours = WorkingTimeCalculator.calcWorkingHours(start, end).longValue();
    assertEquals(expectedHours, actualHours);
  }


  @Test
  public void testCalcWorkingHours_minute() {
    LocalDateTime start = LocalDateTime.of(2024, 10, 30, 10, 0); // 星期三
    LocalDateTime end = LocalDateTime.of(2024, 10, 30, 15, 30);   // 星期三
    long expectedHours = 5; // 3小时  (15 - 10)

    long actualHours = WorkingTimeCalculator.calcWorkingHours(start, end).longValue();
    assertEquals(expectedHours, actualHours);

    double expectedHoursAndMinutes = 5.5; // 3小时  (15 - 10)
    double actualHoursAndMinutes = WorkingTimeCalculator.calcWorkingHours(start, end).doubleValue();
    assertEquals(expectedHoursAndMinutes, actualHoursAndMinutes);
  }

  @Test
  public void testCalcWorkingHours_InvalidInput() {
    LocalDateTime start = LocalDateTime.of(2024, 10, 30, 16, 0); // 星期三
    LocalDateTime end = LocalDateTime.of(2024, 10, 30, 10, 0);   // 星期三
    try {
      WorkingTimeCalculator.calcWorkingHours(start, end);
    } catch (IllegalArgumentException e) {
      assertEquals("The end time must be after the start time", e.getMessage());
    }
  }

  @Test
  public void testCalculateWorkingDays() {
    // 测试用例1：正常情况
    LocalDateTime start1 = LocalDateTime.of(2023, 10, 1, 9, 0); // 周日
    LocalDateTime end1 = LocalDateTime.of(2023, 10, 10, 17, 0); // 周二
    long result1 = WorkingTimeCalculator.calcWorkingDays(start1, end1);
    assertEquals(7, result1, "The number of working days should be 7");

    // 测试用例2：包括一个周末
    LocalDateTime start2 = LocalDateTime.of(2023, 10, 6, 9, 0); // 周五
    LocalDateTime end2 = LocalDateTime.of(2023, 10, 10, 17, 0); // 周二
    long result2 = WorkingTimeCalculator.calcWorkingDays(start2, end2);
    assertEquals(3, result2, "The number of working days should be 3");

    // 测试用例3：没有工作日
    LocalDateTime start3 = LocalDateTime.of(2023, 10, 7, 9, 0); // 周六
    LocalDateTime end3 = LocalDateTime.of(2023, 10, 8, 17, 0); // 周日
    long result3 = WorkingTimeCalculator.calcWorkingDays(start3, end3);
    assertEquals(0, result3, "The number of working days should be 0");

    // 测试用例4：跨越多个周末
    LocalDateTime start4 = LocalDateTime.of(2023, 10, 1, 9, 0); // 周日
    LocalDateTime end4 = LocalDateTime.of(2023, 10, 15, 17, 0); // 周日
    long result4 = WorkingTimeCalculator.calcWorkingDays(start4, end4);
    assertEquals(10, result4, "The number of working days should be 10");
  }

  @Test
  public void testIsToday() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime today = now.withHour(10).withMinute(0).withSecond(0);
    LocalDateTime yesterday = now.minusDays(1);
    LocalDateTime tomorrow = now.plusDays(1);

    assertTrue(WorkingTimeCalculator.isToday(today));
    assertFalse(WorkingTimeCalculator.isToday(yesterday));
    assertFalse(WorkingTimeCalculator.isToday(tomorrow));
  }

  @Test
  public void testIsWithinLastWeek() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime withinLastWeek = now.minusDays(3);
    LocalDateTime moreThanAWeekAgo = now.minusDays(8);

    assertTrue(WorkingTimeCalculator.isLastWeek(withinLastWeek));
    assertFalse(WorkingTimeCalculator.isLastWeek(moreThanAWeekAgo));
  }

  @Test
  public void testIsWithinLastMonth() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime withinLastMonth = now.minusDays(15);
    LocalDateTime moreThanAMonthAgo = now.minusMonths(2);

    assertTrue(WorkingTimeCalculator.isLastMonth(withinLastMonth));
    assertFalse(WorkingTimeCalculator.isLastMonth(moreThanAMonthAgo));
  }
}
