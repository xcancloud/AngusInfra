package cloud.xcan.angus.queue.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.queue.service.QueueService;
import org.junit.jupiter.api.Test;

class DeadLetterMoverTest {

  @Test
  void tickMovesWithConfiguredBatch() {
    QueueService queueService = mock(QueueService.class);
    DeadLetterMover.QueueSettings settings = mock(DeadLetterMover.QueueSettings.class);
    when(settings.getDeadLetterMoveBatch()).thenReturn(33);
    DeadLetterMover mover = new DeadLetterMover(queueService, settings);
    mover.tick();
    verify(queueService).moveExceededAttemptsToDeadLetter(33);
  }
}
