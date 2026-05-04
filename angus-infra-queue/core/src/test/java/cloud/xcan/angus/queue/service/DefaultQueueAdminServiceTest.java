package cloud.xcan.angus.queue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import cloud.xcan.angus.queue.model.DeadLetterData;
import cloud.xcan.angus.queue.model.PartitionCount;
import cloud.xcan.angus.queue.model.StatusCount;
import cloud.xcan.angus.queue.spi.RepositoryAdapter;
import cloud.xcan.angus.queue.spi.SoftDeleteDlqSupport;
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
    when(adapter.countByStatus("t1")).thenReturn(
        List.of(new StatusCount(0, 5L), new StatusCount(1, 2L)));
    when(adapter.deadLetterCountByTopic("t1")).thenReturn(3L);
    when(adapter.readyCountPerPartition("t1")).thenReturn(
        List.of(new PartitionCount(0, 4L), new PartitionCount(1, 1L)));
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
  void topicStatsWithoutAuditLogger() {
    RepositoryAdapter plain = mock(RepositoryAdapter.class);
    when(plain.countByStatus("x")).thenReturn(List.of());
    when(plain.deadLetterCountByTopic("x")).thenReturn(0L);
    when(plain.readyCountPerPartition("x")).thenReturn(List.of());
    DefaultQueueAdminService svc = new DefaultQueueAdminService(plain);
    Map<String, Object> stats = svc.topicStats("x");
    assertNotNull(stats.get("statusCounts"));
    verifyNoInteractions(logger);
  }

  @Test
  void replayFromDeadLetterReturnsZeroWhenEmpty() {
    when(adapter.findDeadLettersByTopicLimit("t1", 5)).thenReturn(List.of());
    assertEquals(0, service.replayFromDeadLetter("t1", 5));
    verify(adapter, never()).saveRecoveredMessages(any());
    verify(logger).adminAction(eq("replayDLQ"), eq("t1"), eq(0), startsWith("limit="));
  }

  @Test
  void purgeDeadLettersHardDeleteWhenSoftEnabledButAdapterNotSupporting() {
    RepositoryAdapter plain = mock(RepositoryAdapter.class);
    DefaultQueueAdminService svc = new DefaultQueueAdminService(plain, logger, true);
    when(plain.purgeDeadLettersByTopic("t1")).thenReturn(2);
    assertEquals(2, svc.purgeDeadLetters("t1"));
    verify(plain).purgeDeadLettersByTopic("t1");
    verify(logger).adminAction(eq("purgeDLQ"), eq("t1"), eq(2), eq(""));
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
    verify(adapter).saveRecoveredMessages(any());
    verify(adapter).deleteDeadLettersByIds(any());
    verify(logger).adminAction(eq("replayDLQ"), eq("t1"), eq(2), startsWith("limit="));
  }
}

