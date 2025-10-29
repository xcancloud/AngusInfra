package cloud.xcan.angus.queue.starter.scheduler;

import cloud.xcan.angus.queue.starter.autoconfigure.QueueProperties;
import cloud.xcan.angus.queue.starter.repository.DeadLetterRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class DlqSoftDeletePurgerScheduler {

  private static final Logger log = LoggerFactory.getLogger(DlqSoftDeletePurgerScheduler.class);
  private final DeadLetterRepository deadLetterRepository;
  private final QueueProperties properties;

  public DlqSoftDeletePurgerScheduler(DeadLetterRepository deadLetterRepository,
      QueueProperties properties) {
    this.deadLetterRepository = deadLetterRepository;
    this.properties = properties;
  }

  @Scheduled(fixedDelayString = "${angus.queue.admin.purge-interval-ms:${angus.queue.admin.purgeIntervalMs:600000}}")
  public void purge() {
    int days = properties.getAdmin().getRetentionDays();
    Instant before = Instant.now().minusSeconds(days * 24L * 3600L);
    int n = deadLetterRepository.purgeSoftDeletedBefore(before);
    if (n > 0) {
      log.info("Purged {} soft-deleted DLQ records older than {} days", n, days);
    } else {
      log.debug("No soft-deleted DLQ records to purge");
    }
  }
}

