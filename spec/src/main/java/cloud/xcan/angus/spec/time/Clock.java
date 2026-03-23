package cloud.xcan.angus.spec.time;

/**
 * Abstraction for reading time, used by rate/moving-average utilities ({@code getTick()} for
 * high-resolution deltas, {@link #getTime()} for wall-clock millis when needed).
 * <p>
 * {@link #getTick()} is typically monotonic (e.g. {@link System#nanoTime()}); it is not comparable
 * across different {@link Clock} implementations. {@link #getTime()} follows epoch wall-clock
 * semantics where overridden (see {@link ManualClock}).
 */
public abstract class Clock {

  protected Clock() {
  }

  /**
   * @return the default clock ({@link UserTimeClock}, based on {@link System#nanoTime()})
   */
  public static Clock defaultClock() {
    return UserTimeClockHolder.DEFAULT;
  }

  /**
   * High-resolution tick suitable for measuring elapsed time on this clock (nanoseconds; semantics
   * depend on the implementation).
   */
  public abstract long getTick();

  /**
   * Wall-clock time in milliseconds since the Unix epoch, unless a subclass defines test/simulated
   * time (e.g. {@link ManualClock}).
   */
  public long getTime() {
    return System.currentTimeMillis();
  }

  /**
   * Default implementation: {@link System#nanoTime()} for {@link #getTick()}, {@link System#currentTimeMillis()}
   * for {@link #getTime()}.
   */
  public static final class UserTimeClock extends Clock {

    @Override
    public long getTick() {
      return System.nanoTime();
    }
  }

  private static final class UserTimeClockHolder {

    private static final Clock DEFAULT = new UserTimeClock();
  }
}
