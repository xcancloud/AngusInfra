package cloud.xcan.angus.queue.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cloud.xcan.angus.queue.autoconfigure.QueueProperties;
import cloud.xcan.angus.queue.service.QueueAdminService;
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
