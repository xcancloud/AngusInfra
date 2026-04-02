package cloud.xcan.angus.queue.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.queue.autoconfigure.QueueProperties;
import cloud.xcan.angus.queue.repository.DeadLetterRepository;
import org.junit.jupiter.api.Test;

class DlqSoftDeletePurgerSchedulerTest {

  @Test
  void purgeCallsRepositoryWithRetentionWindow() {
    DeadLetterRepository repo = mock(DeadLetterRepository.class);
    when(repo.purgeSoftDeletedBefore(any())).thenReturn(0);
    QueueProperties props = new QueueProperties();
    props.getAdmin().setRetentionDays(1);
    DlqSoftDeletePurgerScheduler scheduler = new DlqSoftDeletePurgerScheduler(repo, props);
    scheduler.purge();
    verify(repo).purgeSoftDeletedBefore(any());
  }

  @Test
  void purgeRunsWhenRowsRemoved() {
    DeadLetterRepository repo = mock(DeadLetterRepository.class);
    when(repo.purgeSoftDeletedBefore(any())).thenReturn(2);
    QueueProperties props = new QueueProperties();
    DlqSoftDeletePurgerScheduler scheduler = new DlqSoftDeletePurgerScheduler(repo, props);
    scheduler.purge();
    verify(repo).purgeSoftDeletedBefore(any());
  }
}
