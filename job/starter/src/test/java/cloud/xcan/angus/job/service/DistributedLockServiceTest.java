package cloud.xcan.angus.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.job.entity.DistributedLock;
import cloud.xcan.angus.job.jpa.DistributedLockRepository;
import cloud.xcan.angus.job.properties.JobProperties;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class DistributedLockServiceTest {

  @Mock
  private DistributedLockRepository lockRepository;

  @Mock
  private JobProperties properties;

  @InjectMocks
  private DistributedLockService lockService;

  @BeforeEach
  void setUp() {
    lenient().when(properties.getLockTimeoutSeconds()).thenReturn(30);
  }

  // ---------------------------------------------------------------------------
  // tryLock
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("tryLock returns lockValue when no existing lock")
  void tryLock_success() {
    when(lockRepository.deleteExpiredLockByKey(eq("key1"), any())).thenReturn(0);
    when(lockRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

    String lockValue = lockService.tryLock("key1", "node-A");

    assertThat(lockValue).isNotNull().isNotBlank();
    ArgumentCaptor<DistributedLock> captor = ArgumentCaptor.forClass(DistributedLock.class);
    verify(lockRepository).saveAndFlush(captor.capture());
    DistributedLock saved = captor.getValue();
    assertThat(saved.getLockKey()).isEqualTo("key1");
    assertThat(saved.getOwner()).isEqualTo("node-A");
    assertThat(saved.getLockValue()).isEqualTo(lockValue);
    assertThat(saved.getExpireTime()).isAfter(LocalDateTime.now());
  }

  @Test
  @DisplayName("tryLock returns null when another node holds the lock (constraint violation)")
  void tryLock_contention() {
    when(lockRepository.deleteExpiredLockByKey(any(), any())).thenReturn(0);
    when(lockRepository.saveAndFlush(any()))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    String lockValue = lockService.tryLock("key1", "node-B");

    assertThat(lockValue).isNull();
  }

  // ---------------------------------------------------------------------------
  // unlock
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("unlock deletes lock when owner and lockValue match")
  void unlock_success() {
    DistributedLock lock = new DistributedLock();
    lock.setLockKey("key1");
    lock.setOwner("node-A");
    lock.setLockValue("uuid-123");
    when(lockRepository.findById("key1")).thenReturn(Optional.of(lock));

    lockService.unlock("key1", "node-A", "uuid-123");

    verify(lockRepository).deleteById("key1");
  }

  @Test
  @DisplayName("unlock is rejected when lockValue does not match")
  void unlock_wrongLockValue() {
    DistributedLock lock = new DistributedLock();
    lock.setLockKey("key1");
    lock.setOwner("node-A");
    lock.setLockValue("uuid-correct");
    when(lockRepository.findById("key1")).thenReturn(Optional.of(lock));

    lockService.unlock("key1", "node-A", "uuid-wrong");

    verify(lockRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("unlock is rejected when owner does not match")
  void unlock_wrongOwner() {
    DistributedLock lock = new DistributedLock();
    lock.setLockKey("key1");
    lock.setOwner("node-A");
    lock.setLockValue("uuid-123");
    when(lockRepository.findById("key1")).thenReturn(Optional.of(lock));

    lockService.unlock("key1", "node-B", "uuid-123");

    verify(lockRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("tryLock two-arg delegates default timeout from properties")
  void tryLock_defaultTimeoutOverload() {
    when(lockRepository.deleteExpiredLockByKey(eq("k"), any())).thenReturn(0);
    when(lockRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

    String v = lockService.tryLock("k", "owner");

    assertThat(v).isNotNull();
  }

  @Test
  @DisplayName("tryLock returns null on unexpected exception")
  void tryLock_unexpectedException() {
    when(lockRepository.deleteExpiredLockByKey(any(), any()))
        .thenThrow(new RuntimeException("db down"));

    assertThat(lockService.tryLock("k", "o", 10)).isNull();
  }

  @Test
  @DisplayName("renewLock extends expiry when owner and value match")
  void renewLock_success() {
    DistributedLock lock = new DistributedLock();
    lock.setLockKey("k");
    lock.setOwner("node");
    lock.setLockValue("uuid");
    when(lockRepository.findById("k")).thenReturn(Optional.of(lock));
    when(lockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertThat(lockService.renewLock("k", "node", "uuid", 120)).isTrue();
  }

  @Test
  @DisplayName("renewLock returns false when lock missing or mismatch")
  void renewLock_failure() {
    when(lockRepository.findById("k")).thenReturn(Optional.empty());
    assertThat(lockService.renewLock("k", "n", "v", 10)).isFalse();

    DistributedLock lock = new DistributedLock();
    lock.setOwner("a");
    lock.setLockValue("b");
    when(lockRepository.findById("k2")).thenReturn(Optional.of(lock));
    assertThat(lockService.renewLock("k2", "wrong", "b", 10)).isFalse();
  }

  @Test
  @DisplayName("unlock swallows persistence errors")
  void unlock_exceptionSwallowed() {
    when(lockRepository.findById("k")).thenThrow(new RuntimeException("db"));
    lockService.unlock("k", "o", "v");
  }

  @Test
  @DisplayName("unlock is a no-op when lock has already expired and been cleaned")
  void unlock_lockAlreadyGone() {
    when(lockRepository.findById("key1")).thenReturn(Optional.empty());

    lockService.unlock("key1", "node-A", "any-value");

    verify(lockRepository, never()).deleteById(any());
  }
}
