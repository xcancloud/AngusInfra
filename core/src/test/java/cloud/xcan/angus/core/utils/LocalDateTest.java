package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LocalDateTest {

  @Test
  void testFormat() {
    LocalDate date = LocalDate.of(2021, 9, 1);
    assertEquals("202109", String.format("%4d%02d", date.getYear(), date.getMonthValue()));
    assertEquals(
        "20210901",
        String.format("%4d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
  }
}
