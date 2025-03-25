package cloud.xcan.angus.spec.utils;


public class WaitUtils {

  private boolean status = false;
  private final Object lock = new Object();

  public void waitForStatus(boolean expectedStatus, long timeoutMillis)
      throws InterruptedException {
    synchronized (lock) {
      long startTime = System.currentTimeMillis();
      long elapsedTime = 0;

      while (status != expectedStatus && elapsedTime < timeoutMillis) {
        lock.wait(timeoutMillis - elapsedTime);
        elapsedTime = System.currentTimeMillis() - startTime;
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
