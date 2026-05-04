package cloud.xcan.angus.spec.time;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * A clock whose {@link #getTick()} returns the <em>current thread</em> CPU time in nanoseconds
 * (when supported). {@link #getTime()} remains wall-clock millis from {@link Clock#getTime()}.
 * <p>
 * If CPU time is not supported, {@link ThreadMXBean#getCurrentThreadCpuTime()} may return
 * {@code -1}; callers should handle that case if relevant.
 */
public final class CpuTimeClock extends Clock {

  private static final ThreadMXBean THREAD_MX_BEAN = initMxBean();

  private static ThreadMXBean initMxBean() {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    try {
      if (bean.isThreadCpuTimeSupported() && !bean.isThreadCpuTimeEnabled()) {
        bean.setThreadCpuTimeEnabled(true);
      }
    } catch (SecurityException | UnsupportedOperationException ignored) {
      // JVM may disallow enabling; getCurrentThreadCpuTime() may still work or return -1
    }
    return bean;
  }

  @Override
  public long getTick() {
    return THREAD_MX_BEAN.getCurrentThreadCpuTime();
  }
}
