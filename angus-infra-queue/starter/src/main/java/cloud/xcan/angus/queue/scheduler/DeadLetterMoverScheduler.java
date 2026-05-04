package cloud.xcan.angus.queue.scheduler;

import cloud.xcan.angus.queue.autoconfigure.QueueProperties;
import cloud.xcan.angus.queue.service.QueueService;
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

