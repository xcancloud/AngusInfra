package cloud.xcan.angus.spec.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The default thread factory.
 */
public class DefaultThreadFactory implements ThreadFactory {

  private static final AtomicInteger POOL_NUM = new AtomicInteger(1);
  private final ThreadGroup group;
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
    SecurityManager s = System.getSecurityManager();
    this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    this.namePrefix = prefix + POOL_NUM.getAndIncrement() + "-Thread-";
    this.daemon = daemon;
    this.priority = priority;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
    t.setDaemon(this.daemon);
    t.setPriority(this.priority);
    return t;
  }
}
