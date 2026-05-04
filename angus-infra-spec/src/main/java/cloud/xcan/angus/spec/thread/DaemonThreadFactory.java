package cloud.xcan.angus.spec.thread;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory that creates only daemon threads with stable names for diagnostics.
 */
public enum DaemonThreadFactory implements ThreadFactory {
  INSTANCE;

  private static final AtomicInteger THREAD_NUM = new AtomicInteger(1);

  @Override
  public Thread newThread(Runnable r) {
    Objects.requireNonNull(r, "r");
    int n = THREAD_NUM.getAndIncrement();
    return Thread.ofPlatform()
        .name("angus-daemon-", n)
        .daemon(true)
        .unstarted(r);
  }
}
