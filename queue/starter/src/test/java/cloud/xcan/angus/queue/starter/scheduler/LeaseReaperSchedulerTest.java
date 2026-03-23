package cloud.xcan.angus.queue.starter.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cloud.xcan.angus.queue.core.service.QueueAdminService;
import cloud.xcan.angus.queue.starter.autoconfigure.QueueProperties;
import org.junit.jupiter.api.Test;

class LeaseReaperSchedulerTest {

  @Test
  void reclaimDelegatesToAdminServiceWithBatchFromProperties() {
    QueueAdminService admin = mock(QueueAdminService.class);
    QueueProperties props = new QueueProperties();
    props.setReclaimBatch(77);
    LeaseReaperScheduler scheduler = new LeaseReaperScheduler(admin, props);
    scheduler.reclaim();
    verify(admin).reclaimExpired(77);
  }
}
