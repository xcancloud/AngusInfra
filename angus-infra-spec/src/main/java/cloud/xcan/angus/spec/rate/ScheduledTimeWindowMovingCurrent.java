package cloud.xcan.angus.spec.rate;

import cloud.xcan.angus.spec.thread.DefaultThreadFactory;
import cloud.xcan.angus.spec.time.Clock;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class ScheduledTimeWindowMovingCurrent implements MovingCurrent {

  protected static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(1);
  private final LongAdder currentCount = new LongAdder();
  private final Clock clock;
  private volatile long startTime;
  private volatile double s1Rate = 0.0;
  private volatile double maxS1Rate = 0.0;

  /**
   * Creates a new {@link ScheduledTimeWindowMovingCurrent}.
   */
  public ScheduledTimeWindowMovingCurrent() {
    this(Clock.defaultClock());
  }

  /**
   * Creates a new {@link ScheduledTimeWindowMovingCurrent}.
   *
   * @param clock the clock to use for the meter ticks
   */
  public ScheduledTimeWindowMovingCurrent(Clock clock) {
    this.clock = clock;
    this.startTime = clock.getTick();
    ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(1,
        new DefaultThreadFactory("Metrics-Moving-Schedule", Thread.NORM_PRIORITY + 1));
    schedule.scheduleAtFixedRate(this::tickIfNecessary, 1, TICK_INTERVAL, TimeUnit.NANOSECONDS);
  }

  @Override
  public void tickIfNecessary() {
    final long currentTime = clock.getTick();
    final long interval = currentTime - startTime;
    if (interval <= TICK_INTERVAL) {
      s1Rate = currentCount.doubleValue();
    } else {
      s1Rate = (double) currentCount.sumThenReset() / interval * TICK_INTERVAL;
      startTime = currentTime;
    }
    if (s1Rate > maxS1Rate) {
      maxS1Rate = s1Rate;
    }
  }

  @Override
  public void update(long n) {
    currentCount.add(n);
  }

  @Override
  public double getS1Rate() {
    return s1Rate;
  }

  @Override
  public double getMaxS1Rate() {
    return maxS1Rate;
  }

}
