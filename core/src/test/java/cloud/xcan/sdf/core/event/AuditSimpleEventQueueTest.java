package cloud.xcan.sdf.core.event;

import static cloud.xcan.sdf.core.disruptor.DisruptorQueueFactory.createHandleEventsQueue;
import static cloud.xcan.sdf.core.disruptor.DisruptorQueueFactory.createWorkPoolQueue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cloud.xcan.sdf.core.disruptor.DisruptorQueueManager;
import cloud.xcan.sdf.spec.EventObject;
import cloud.xcan.sdf.spec.thread.DefaultThreadFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class AuditSimpleEventQueueTest {

  @Test
  public void createWorkPoolQueueTest() throws InterruptedException {
    CountDownLatch downLatch = new CountDownLatch(1);
    Map<String, String> source = new HashMap<>();
    AtomicInteger consumerCount = new AtomicInteger(0);
    DisruptorQueueManager<String> queue = createWorkPoolQueue(
        128 * 1024, true,
        new DefaultThreadFactory("Test", Thread.NORM_PRIORITY - 1),
        new EventListener<String>() {
          @Override
          public void onEvent(EventObject<String> event) {
            System.out.println(event);
            source.put("source", event.getSource());
            consumerCount.incrementAndGet();
            downLatch.countDown();
          }

          @Override
          public void onEvent(EventObject<String> event, long sequence, boolean endOfBatch)
              throws Exception {
            this.onEvent(event);
          }
        }, new EventListener<String>() {
          @Override
          public void onEvent(EventObject<String> event) {
            System.out.println(event);
            source.put("source", event.getSource());
            consumerCount.incrementAndGet();
            downLatch.countDown();
          }

          @Override
          public void onEvent(EventObject<String> event, long sequence, boolean endOfBatch)
              throws Exception {
            this.onEvent(event);
          }
        }
    );
    assertNotNull(queue);
    assertNotNull(queue.getDisruptor());
    assertNotNull(queue.getRingBuffer());
    //Assert.assertEquals(-1L, queue.cursor());
    queue.add("Ping");
    downLatch.await();
    assertEquals("Ping", source.get("source"));
    assertEquals(1, consumerCount.get());
    assertEquals(0L, queue.cursor());
  }


  @Test
  public void createHandleEventsQueueTest() throws InterruptedException {
    CountDownLatch downLatch = new CountDownLatch(2);
    Map<String, String> source = new HashMap<>();
    AtomicInteger consumerCount = new AtomicInteger(0);
    DisruptorQueueManager<String> queue = createHandleEventsQueue(
        128 * 1024, true,
        new DefaultThreadFactory("Test", Thread.NORM_PRIORITY - 1),
        new EventListener<String>() {
          @Override
          public void onEvent(EventObject<String> event) {
            System.out.println(event);
            source.put("source", event.getSource());
            consumerCount.incrementAndGet();
            downLatch.countDown();
          }

          @Override
          public void onEvent(EventObject<String> event, long sequence, boolean endOfBatch)
              throws Exception {
            this.onEvent(event);
          }
        }, new EventListener<String>() {
          @Override
          public void onEvent(EventObject<String> event) {
            System.out.println(event);
            source.put("source", event.getSource());
            consumerCount.incrementAndGet();
            downLatch.countDown();
          }

          @Override
          public void onEvent(EventObject<String> event, long sequence, boolean endOfBatch)
              throws Exception {
            this.onEvent(event);
          }
        }
    );
    assertNotNull(queue);
    assertNotNull(queue.getDisruptor());
    assertNotNull(queue.getRingBuffer());
    //Assert.assertEquals(-1L, queue.cursor());
    queue.add("Ping");
    downLatch.await();
    assertEquals("Ping", source.get("source"));
    assertEquals(2, consumerCount.get());
    assertEquals(0L, queue.cursor());
  }

}

