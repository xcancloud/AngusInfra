package cloud.xcan.angus.queue.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import cloud.xcan.angus.queue.core.model.DeadLetterData;
import cloud.xcan.angus.queue.core.spi.RepositoryAdapter;
import cloud.xcan.angus.queue.core.spi.SoftDeleteDlqSupport;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultQueueAdminServiceTest {

  RepositoryAdapter adapter;
  DefaultQueueAdminService service;
  AuditLogger logger;

  @BeforeEach
  void setup() {
    adapter = mock(RepositoryAdapter.class,
        withSettings().extraInterfaces(SoftDeleteDlqSupport.class));
    logger = mock(AuditLogger.class);
    service = new DefaultQueueAdminService(adapter, logger, true);
  }

  @Test
  void topicStatsAggregatesCounts() {
    when(adapter.countByStatus("t1")).thenReturn(List.of(new Object[]{0, 5L}, new Object[]{1, 2L}));
    when(adapter.deadLetterCountByTopic("t1")).thenReturn(3L);
    when(adapter.readyCountPerPartition("t1")).thenReturn(
        List.of(new Object[]{0, 4L}, new Object[]{1, 1L}));
    Map<String, Object> stats = service.topicStats("t1");
    assertEquals(2, ((Map<?, ?>) stats.get("statusCounts")).size());
    assertEquals(3L, stats.get("dlqCount"));
    assertEquals(2, ((Map<?, ?>) stats.get("readyPerPartition")).size());
    verify(logger).adminAction(eq("stats"), eq("t1"), eq(0), eq(""));
  }

  @Test
  void reclaimExpiredDelegatesAndLogs() {
    when(adapter.reclaimExpiredLeases(100)).thenReturn(8);
    int n = service.reclaimExpired(100);
    assertEquals(8, n);
    verify(logger).adminAction(eq("reclaimExpired"), isNull(), eq(8), startsWith("limit="));
  }

  @Test
  void purgeDoneDelegatesAndLogs() {
    Instant before = Instant.parse("2024-01-01T00:00:00Z");
    when(adapter.purgeDoneBefore("t1", before)).thenReturn(9);
    int n = service.purgeDone("t1", before);
    assertEquals(9, n);
    verify(logger).adminAction(eq("purgeDone"), eq("t1"), eq(9), anyString());
  }

  @Test
  void purgeDeadLettersSoftDeleteWhenSupported() {
    SoftDeleteDlqSupport s = (SoftDeleteDlqSupport) adapter;
    when(s.softDeleteDeadLettersByTopic("t1")).thenReturn(6);
    int n = service.purgeDeadLetters("t1");
    assertEquals(6, n);
    verify(logger).adminAction(eq("softDeleteDLQ"), eq("t1"), eq(6), eq(""));
  }

  @Test
  void purgeDeadLettersHardDeleteWhenNotSoft() {
    DefaultQueueAdminService svc = new DefaultQueueAdminService(adapter, logger, false);
    when(adapter.purgeDeadLettersByTopic("t1")).thenReturn(4);
    int n = svc.purgeDeadLetters("t1");
    assertEquals(4, n);
    verify(logger).adminAction(eq("purgeDLQ"), eq("t1"), eq(4), eq(""));
  }

  @Test
  void replayFromDeadLetterMovesMessages() {
    DeadLetterData d1 = new DeadLetterData();
    d1.setId(10L);
    d1.setTopic("t1");
    DeadLetterData d2 = new DeadLetterData();
    d2.setId(11L);
    d2.setTopic("t1");
    when(adapter.findDeadLettersByTopicLimit("t1", 2)).thenReturn(List.of(d1, d2));
    int n = service.replayFromDeadLetter("t1", 2);
    assertEquals(2, n);
    verify(adapter, times(2)).saveRecoveredMessage(any());
    verify(adapter).deleteDeadLetterById(10L);
    verify(adapter).deleteDeadLetterById(11L);
    verify(logger).adminAction(eq("replayDLQ"), eq("t1"), eq(2), startsWith("limit="));
  }
}

