package cloud.xcan.angus.queue.starter.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cloud.xcan.angus.queue.core.service.QueueService;
import cloud.xcan.angus.queue.starter.autoconfigure.QueueProperties;
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
