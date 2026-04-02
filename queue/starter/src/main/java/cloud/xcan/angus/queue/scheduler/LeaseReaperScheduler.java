package cloud.xcan.angus.queue.scheduler;

import cloud.xcan.angus.queue.autoconfigure.QueueProperties;
import cloud.xcan.angus.queue.service.QueueAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class LeaseReaperScheduler {

  private static final Logger log = LoggerFactory.getLogger(LeaseReaperScheduler.class);
  private final LeaseReaper core;

  public LeaseReaperScheduler(QueueAdminService adminService, QueueProperties props) {
    this.core = new LeaseReaper(adminService, props::getReclaimBatch);
  }

  @Scheduled(fixedDelayString = "${angus.queue.reclaim-interval-ms:3000}")
  public void reclaim() {
    core.tick();
    log.debug("Reclaimed expired leases tick executed");
  }
}

