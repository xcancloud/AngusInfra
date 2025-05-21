package cloud.xcan.angus.core.event;

import static cloud.xcan.angus.core.disruptor.DisruptorQueueFactory.createWorkPoolQueue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cloud.xcan.angus.core.disruptor.DisruptorQueueManager;
import cloud.xcan.angus.core.event.source.UserOperation;
import cloud.xcan.angus.spec.thread.DefaultThreadFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

public class OperationSimpleEventTest {

  @Test
  public void produceAndConsumeTest() throws InterruptedException {
    CountDownLatch downLatch = new CountDownLatch(1);
    List<OperationEvent> store = new ArrayList<>();
    DisruptorQueueManager<OperationEvent> queue = createWorkPoolQueue(
        128 * 1024, true,
        new DefaultThreadFactory("Test", Thread.NORM_PRIORITY - 1),
        new OperationEventListener<>(
            new EventRepository<OperationEvent>() {

              @Override
              public void add(OperationEvent event) {
                downLatch.countDown();
                store.add(event);
              }

              @Override
              public void add(List<OperationEvent> events) {
                store.addAll(events);
              }

              @Override
              public List<OperationEvent> find(String principal, Instant after, String type) {
                return store;
              }
            })
    );
    assertNotNull(queue);
    assertNotNull(queue.getDisruptor());
    assertNotNull(queue.getRingBuffer());
    //Assert.assertEquals(-1L, queue.cursor());
    OperationEvent operationEvent = new OperationEvent(UserOperation.newBuilder().build());
    queue.add(operationEvent);
    downLatch.await();
    assertEquals(operationEvent, store.get(0));
  }

}
