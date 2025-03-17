package cloud.xcan.sdf.spec.rate;


import static cloud.xcan.sdf.spec.rate.ExponentialMovingAverages.TICK_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cloud.xcan.sdf.spec.time.ManualClock;
import org.junit.Test;

public class ExponentialMovingAveragesTest {

  @Test
  public void rate() {
    ManualClock clock = new ManualClock();
    ExponentialMovingAverages movingAverages = new ExponentialMovingAverages(clock);
    movingAverages.update(10);
    clock.addNanos(TICK_INTERVAL + 1);
    movingAverages.tickIfNecessary();
    assertEquals(2d, movingAverages.getM1Rate(), 2);
    System.out.println(movingAverages.getM1Rate());
    System.out.println(movingAverages.getM5Rate());
    clock.addNanos(TICK_INTERVAL + 1);
    movingAverages.tickIfNecessary();
    System.out.println(movingAverages.getM1Rate());
    System.out.println(movingAverages.getM5Rate());
    movingAverages.update(1000);
    clock.addNanos(TICK_INTERVAL + 1);
    movingAverages.tickIfNecessary();
    System.out.println(movingAverages.getM1Rate());
    System.out.println(movingAverages.getM5Rate());
  }

}
