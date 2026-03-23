package cloud.xcan.angus.spec.time;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A test-oriented clock: {@link #getTick()} returns a value advanced only via {@code add*} /
 * {@link #setNanos(long)} / {@link #reset()}. {@link #getTime()} returns simulated epoch millis
 * derived from the tick offset from {@code initialTicksInNanos}.
 */
public final class ManualClock extends Clock {

  private final long initialTicksInNanos;
  private long ticksInNanos;

  public ManualClock(long initialTicksInNanos) {
    this.initialTicksInNanos = initialTicksInNanos;
    this.ticksInNanos = initialTicksInNanos;
  }

  public ManualClock() {
    this(0L);
  }

  public synchronized void addNanos(long nanos) {
    ticksInNanos += nanos;
  }

  public synchronized void addSeconds(long seconds) {
    ticksInNanos += TimeUnit.SECONDS.toNanos(seconds);
  }

  public synchronized void addMillis(long millis) {
    ticksInNanos += TimeUnit.MILLISECONDS.toNanos(millis);
  }

  public synchronized void addHours(long hours) {
    ticksInNanos += TimeUnit.HOURS.toNanos(hours);
  }

  /** Advances the tick by the given duration (nanosecond precision). */
  public synchronized void add(Duration duration) {
    Objects.requireNonNull(duration, "duration");
    ticksInNanos += duration.toNanos();
  }

  /** Sets the absolute tick in nanoseconds (same basis as {@link #getTick()}). */
  public synchronized void setNanos(long nanos) {
    ticksInNanos = nanos;
  }

  /** Restores the tick to the initial value passed to the constructor. */
  public synchronized void reset() {
    ticksInNanos = initialTicksInNanos;
  }

  @Override
  public synchronized long getTick() {
    return ticksInNanos;
  }

  /**
   * Simulated wall millis: {@code NANOSECONDS.toMillis(getTick() - initialTicksInNanos)}.
   */
  @Override
  public synchronized long getTime() {
    return TimeUnit.NANOSECONDS.toMillis(ticksInNanos - initialTicksInNanos);
  }
}
