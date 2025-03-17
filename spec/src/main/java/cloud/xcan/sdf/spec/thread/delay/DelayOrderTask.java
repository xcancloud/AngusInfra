package cloud.xcan.sdf.spec.thread.delay;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayOrderTask<T extends Runnable> implements Delayed {

  private final long timeout;
  private final T task;

  public DelayOrderTask(long timeout, T task) {
    this.timeout = System.nanoTime() + timeout;
    this.task = task;
  }

  @Override
  public int compareTo(Delayed o) {
    DelayOrderTask other = (DelayOrderTask) o;
    long diff = timeout - other.timeout;
    if (diff > 0) {
      return 1;
    } else if (diff < 0) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(this.timeout - System.nanoTime(), TimeUnit.NANOSECONDS);
  }

  @Override
  public int hashCode() {
    return task.hashCode();
  }

  public T getTask() {
    return task;
  }
}  