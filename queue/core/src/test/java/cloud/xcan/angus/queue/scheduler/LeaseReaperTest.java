package cloud.xcan.angus.queue.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.queue.service.QueueAdminService;
import org.junit.jupiter.api.Test;

class LeaseReaperTest {

  @Test
  void tickReclaimsWithConfiguredBatch() {
    QueueAdminService admin = mock(QueueAdminService.class);
    LeaseReaper.QueueSettings settings = mock(LeaseReaper.QueueSettings.class);
    when(settings.getReclaimBatch()).thenReturn(42);
    LeaseReaper reaper = new LeaseReaper(admin, settings);
    reaper.tick();
    verify(admin).reclaimExpired(42);
  }
}
