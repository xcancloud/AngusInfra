package cloud.xcan.angus.queue.core.scheduler;

import cloud.xcan.angus.queue.core.service.QueueAdminService;

public class LeaseReaper {

  private final QueueAdminService adminService;
  private final QueueSettings settings;

  public LeaseReaper(QueueAdminService adminService, QueueSettings settings) {
    this.adminService = adminService;
    this.settings = settings;
  }

  public void tick() {
    adminService.reclaimExpired(settings.getReclaimBatch());
  }

  public interface QueueSettings {

    int getReclaimBatch();
  }
}

