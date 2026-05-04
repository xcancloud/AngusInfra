package cloud.xcan.angus.queue.scheduler;

import cloud.xcan.angus.queue.service.QueueService;

public class DeadLetterMover {

  private final QueueService queueService;
  private final QueueSettings settings;

  public DeadLetterMover(QueueService queueService, QueueSettings settings) {
    this.queueService = queueService;
    this.settings = settings;
  }

  public void tick() {
    queueService.moveExceededAttemptsToDeadLetter(settings.getDeadLetterMoveBatch());
  }

  public interface QueueSettings {

    int getDeadLetterMoveBatch();
  }
}

