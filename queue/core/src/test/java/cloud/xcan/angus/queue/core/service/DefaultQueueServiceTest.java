package cloud.xcan.angus.queue.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.queue.core.model.MessageData;
import cloud.xcan.angus.queue.core.spi.RepositoryAdapter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultQueueServiceTest {

  RepositoryAdapter adapter;
  DefaultQueueService service;

  @BeforeEach
  void setUp() {
    adapter = mock(RepositoryAdapter.class);
    service = new DefaultQueueService(adapter);
  }

  @Test
  void sendDelegatesToAdapter() {
    when(
        adapter.saveMessage(any(), any(), any(), any(), anyInt(), any(), any(), anyInt(), anyInt()))
        .thenReturn(123L);
    Long id = service.send("topicA", "key1", "payload", "{}", 1, Instant.EPOCH, "idem1", 5, 3);
    assertEquals(123L, id);
    verify(adapter).saveMessage(eq("topicA"), eq("key1"), eq("payload"), eq("{}"), eq(1),
        eq(Instant.EPOCH), eq("idem1"), eq(5), eq(3));
  }

  @Test
  void leaseDelegatesToAdapter() {
    when(adapter.leaseBatch(any(), any(), any(), anyInt(), anyInt())).thenReturn(2);
    int leased = service.lease("topicA", List.of(0, 1), "ownerA", 30, 10);
    assertEquals(2, leased);
    verify(adapter).leaseBatch(eq("topicA"), eq(List.of(0, 1)), eq("ownerA"), eq(30), eq(10));
  }

  @Test
  void listLeasedByOwnerUsesNowAndLimit() {
    when(adapter.findLeasedByOwner(any(), any(), anyInt())).thenReturn(List.of(new MessageData()));
    List<MessageData> result = service.listLeasedByOwner("ownerA", 5);
    assertEquals(1, result.size());
    verify(adapter).findLeasedByOwner(eq("ownerA"), any(Instant.class), eq(5));
  }

  @Test
  void ackSkipsWhenIdsEmpty() {
    int n = service.ack(List.of());
    assertEquals(0, n);
    verify(adapter, never()).ackBatch(any());
  }

  @Test
  void ackDelegates() {
    when(adapter.ackBatch(any())).thenReturn(3);
    int n = service.ack(List.of(1L, 2L));
    assertEquals(3, n);
    verify(adapter).ackBatch(eq(List.of(1L, 2L)));
  }

  @Test
  void nackSkipsWhenIdsEmpty() {
    int n = service.nack(List.of(), 10);
    assertEquals(0, n);
    verify(adapter, never()).nackBatch(any(), anyInt());
  }

  @Test
  void nackDelegates() {
    when(adapter.nackBatch(any(), anyInt())).thenReturn(2);
    int n = service.nack(List.of(1L, 2L), 10);
    assertEquals(2, n);
    verify(adapter).nackBatch(eq(List.of(1L, 2L)), eq(10));
  }

  @Test
  void moveExceededAttemptsDelegates() {
    when(adapter.moveExceededToDeadLetter(anyInt())).thenReturn(7);
    int n = service.moveExceededAttemptsToDeadLetter(50);
    assertEquals(7, n);
    verify(adapter).moveExceededToDeadLetter(eq(50));
  }
}

