package cloud.xcan.angus.queue.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cloud.xcan.angus.queue.autoconfigure.QueueProperties;
import cloud.xcan.angus.queue.service.QueueService;
import org.junit.jupiter.api.Test;

class DeadLetterMoverSchedulerTest {

  @Test
  void moveDelegatesToQueueServiceWithBatchFromProperties() {
    QueueService queueService = mock(QueueService.class);
    QueueProperties props = new QueueProperties();
    props.setDeadLetterMoveBatch(55);
    DeadLetterMoverScheduler scheduler = new DeadLetterMoverScheduler(queueService, props);
    scheduler.move();
    verify(queueService).moveExceededAttemptsToDeadLetter(55);
  }
}
