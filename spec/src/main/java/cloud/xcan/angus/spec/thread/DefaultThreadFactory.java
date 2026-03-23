package cloud.xcan.angus.spec.thread;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NonNull;

/**
 * The default thread factory (JDK 21 {@link Thread#ofPlatform()}).
 */
public class DefaultThreadFactory implements ThreadFactory {

  private static final AtomicInteger POOL_NUM = new AtomicInteger(1);
  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final String namePrefix;
  private final boolean daemon;
  private final int priority;

  public DefaultThreadFactory(String prefix) {
    this(prefix, false, Thread.NORM_PRIORITY);
  }

  public DefaultThreadFactory(String prefix, boolean daemon) {
    this(prefix, daemon, Thread.NORM_PRIORITY);
  }

  public DefaultThreadFactory(String prefix, int priority) {
    this(prefix, false, priority);
  }

  public DefaultThreadFactory(String prefix, boolean daemon, int priority) {
    Objects.requireNonNull(prefix, "prefix");
    if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
      throw new IllegalArgumentException(
          "priority must be in [" + Thread.MIN_PRIORITY + ", " + Thread.MAX_PRIORITY + "]: " + priority);
    }
    this.namePrefix = prefix + POOL_NUM.getAndIncrement() + "-Thread-";
    this.daemon = daemon;
    this.priority = priority;
  }

  @Override
  public Thread newThread(@NonNull Runnable r) {
    int n = threadNumber.getAndIncrement();
    return Thread.ofPlatform()
        .name(namePrefix, n)
        .daemon(daemon)
        .priority(priority)
        .unstarted(r);
  }
}
