package cloud.xcan.angus.queue.starter.scheduler;

import cloud.xcan.angus.queue.core.scheduler.DeadLetterMover;
import cloud.xcan.angus.queue.core.service.QueueService;
import cloud.xcan.angus.queue.starter.autoconfigure.QueueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class DeadLetterMoverScheduler {

  private static final Logger log = LoggerFactory.getLogger(DeadLetterMoverScheduler.class);
  private final DeadLetterMover core;

  public DeadLetterMoverScheduler(QueueService queueService, QueueProperties props) {
    this.core = new DeadLetterMover(queueService, props::getDeadLetterMoveBatch);
  }

  @Scheduled(fixedDelayString = "${angus.queue.dead-letter-move-interval-ms:5000}")
  public void move() {
    core.tick();
    log.debug("Moved DLQ tick executed");
  }
}

