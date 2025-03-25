package cloud.xcan.angus.spec.thread;

import java.util.concurrent.ThreadFactory;

/**
 * Access to a ThreadFactory instance. All threads are created with setDaemon(true).
 */
public enum DaemonThreadFactory implements ThreadFactory {
  INSTANCE;

  @Override
  public Thread newThread(final Runnable r) {
    Thread t = new Thread(r);
    t.setDaemon(true);
    return t;
  }
}
