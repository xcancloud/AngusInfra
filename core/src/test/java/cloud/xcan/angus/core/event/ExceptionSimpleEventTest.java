package cloud.xcan.angus.core.event;

import static cloud.xcan.angus.core.disruptor.DisruptorQueueFactory.createWorkPoolQueue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cloud.xcan.angus.core.disruptor.DisruptorQueueManager;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.spec.thread.DefaultThreadFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

public class ExceptionSimpleEventTest {

  @Test
  public void produceAndConsumeTest() throws InterruptedException {
    CountDownLatch downLatch = new CountDownLatch(1);
    List<CommonEvent> store = new ArrayList<>();
    DisruptorQueueManager<CommonEvent> queue = createWorkPoolQueue(
        128 * 1024, true,
        new DefaultThreadFactory("Test", Thread.NORM_PRIORITY - 1),
        new EventsListener<>(
            new EventRepository<CommonEvent>() {

              @Override
              public void add(CommonEvent event) {
                downLatch.countDown();
                store.add(event);
              }

              @Override
              public void add(List<CommonEvent> events) {
                store.addAll(events);
              }

              @Override
              public List<CommonEvent> find(String principal, Instant after, String type) {
                return store;
              }
            })
    );
    assertNotNull(queue);
    assertNotNull(queue.getDisruptor());
    assertNotNull(queue.getRingBuffer());
    //Assert.assertEquals(-1L, queue.cursor());
    CommonEvent commonEvent = new CommonEvent(EventContent.newBuilder().build());
    queue.add(commonEvent);
    downLatch.await();
    assertEquals(commonEvent, store.get(0));
  }

}
