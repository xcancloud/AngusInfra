package cloud.xcan.sdf.spec.rate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cloud.xcan.sdf.spec.time.ManualClock;
import org.junit.Test;

public class TimeWindowMovingCurrentTest {

  @Test
  public void rate() throws InterruptedException {
    ManualClock clock = new ManualClock();
    TimeWindowMovingCurrent movingCur = new TimeWindowMovingCurrent(clock);
    clock.addMillis(2);
    movingCur.update(10);
    movingCur.tickIfNecessary();
    assertEquals(10d, movingCur.getS1Rate(), 2);
    assertEquals(10d, movingCur.getMaxS1Rate(), 2);
    clock.addSeconds(2);
    movingCur.tickIfNecessary();
    assertEquals(5d, movingCur.getS1Rate(), 2);
    assertEquals(10d, movingCur.getMaxS1Rate(), 2);
  }

}
