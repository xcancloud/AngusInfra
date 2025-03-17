package cloud.xcan.sdf.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.Test;

public class LocalDateTest {

  @Test
  public void testFormat() {
    LocalDate now = LocalDate.of(2021, 9, 1);
    assertEquals("202109", String.format("%4d%02d", now.getYear(), now.getMonthValue()));
    assertEquals("20210901",
        String.format("%4d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth()));

  }

}