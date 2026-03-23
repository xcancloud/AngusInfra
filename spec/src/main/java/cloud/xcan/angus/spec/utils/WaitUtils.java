package cloud.xcan.angus.spec.utils;


/**
 * Coordinates a boolean flag with {@link Object#wait(long)} / {@link Object#notifyAll()} for
 * simple handoff between threads. Waits are bounded by a wall-clock timeout.
 */
public class WaitUtils {

  private boolean status = false;
  private final Object lock = new Object();

  public void waitForStatus(boolean expectedStatus, long timeoutMillis)
      throws InterruptedException {
    if (timeoutMillis <= 0) {
      return;
    }
    synchronized (lock) {
      long deadline = System.currentTimeMillis() + timeoutMillis;
      while (status != expectedStatus) {
        long remaining = deadline - System.currentTimeMillis();
        if (remaining <= 0) {
          break;
        }
        lock.wait(remaining);
      }
    }
  }

  public void setStatus(boolean newStatus) {
    synchronized (lock) {
      status = newStatus;
      lock.notifyAll();
    }
  }
}
