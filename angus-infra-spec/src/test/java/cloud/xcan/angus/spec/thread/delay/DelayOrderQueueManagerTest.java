package cloud.xcan.angus.spec.thread.delay;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DelayOrderQueueManagerTest {

  static DelayOrderQueueManager queueManager;

  @BeforeClass
  public static void setup() {
    queueManager = new DelayOrderQueueManager();
  }

  @Test
  public void testDelayTask() throws InterruptedException {
    long delay = 200L;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    Event event = new Event(countDownLatch);
    queueManager.put(event, delay, TimeUnit.MILLISECONDS);
    countDownLatch.await();
    long actualDelay = event.getEndMs() - event.getStartMs();
    System.out.println(actualDelay);
    Assert.assertTrue(actualDelay >= delay);
  }

  @AfterClass
  public static void teardown() {
    queueManager.stop();
  }

  class Event implements Runnable {

    CountDownLatch countDownLatch;
    long startMs;
    long endMs;

    public Event(CountDownLatch countDownLatch) {
      this.countDownLatch = countDownLatch;
      this.startMs = System.currentTimeMillis();
    }

    @Override
    public void run() {
      System.out.println(Thread.currentThread().getName());
      endMs = System.currentTimeMillis();
      countDownLatch.countDown();
    }

    public long getStartMs() {
      return startMs;
    }

    public long getEndMs() {
      return endMs;
    }
  }
}
