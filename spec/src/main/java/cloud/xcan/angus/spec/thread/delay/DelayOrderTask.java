package cloud.xcan.angus.spec.thread.delay;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Delayed} wrapper around a {@link Runnable}. The delay is measured in nanoseconds from
 * construction: execution is due at {@code System.nanoTime() + delayNanos}.
 *
 * @param <T> runnable implementation type
 */
public final class DelayOrderTask<T extends Runnable> implements Delayed {

  private final long fireAtNanos;
  private final T task;

  /**
   * @param delayNanos delay from now until the task is due ({@link TimeUnit#NANOSECONDS})
   * @param task       non-null runnable to run when due
   */
  public DelayOrderTask(long delayNanos, T task) {
    this.fireAtNanos = System.nanoTime() + delayNanos;
    this.task = Objects.requireNonNull(task, "task");
  }

  @Override
  public int compareTo(Delayed o) {
    if (!(o instanceof DelayOrderTask<?> other)) {
      throw new ClassCastException(
          "Expected DelayOrderTask, got " + o.getClass().getName());
    }
    return Long.compare(this.fireAtNanos, other.fireAtNanos);
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(fireAtNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
  }

  @Override
  public int hashCode() {
    return Objects.hash(task, fireAtNanos);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DelayOrderTask<?> that)) {
      return false;
    }
    return fireAtNanos == that.fireAtNanos && task.equals(that.task);
  }

  public T getTask() {
    return task;
  }
}
