package cloud.xcan.angus.spec.rate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cloud.xcan.angus.spec.time.ManualClock;
import org.junit.Test;

public class SlidingTimeWindowMovingAveragesTest {

  @Test
  public void rate() throws InterruptedException {
    ManualClock clock = new ManualClock();
    SlidingTimeWindowMovingAverages movingAverages = new SlidingTimeWindowMovingAverages(clock);
    clock.addMillis(2);
    movingAverages.update(10);
    assertEquals(10d, movingAverages.getM1Rate(), 2);
    System.out.println(movingAverages.getM5Rate());
    clock.addSeconds(2);
    assertEquals(10d, movingAverages.getM1Rate(), 2);
    System.out.println(movingAverages.getM5Rate());
    clock.addSeconds(61);
    assertEquals(10d, movingAverages.getM1Rate(), 2);
    System.out.println(movingAverages.getM5Rate());
  }

}
