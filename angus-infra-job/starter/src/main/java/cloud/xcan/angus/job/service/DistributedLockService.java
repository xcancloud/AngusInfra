package cloud.xcan.angus.job.service;

import cloud.xcan.angus.job.entity.DistributedLock;
import cloud.xcan.angus.job.jpa.DistributedLockRepository;
import cloud.xcan.angus.job.properties.JobProperties;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database-backed distributed lock service.
 *
 * <h3>Atomicity guarantee</h3>
 * Lock acquisition is performed in two steps within a single transaction:
 * <ol>
 *   <li>Delete any <em>expired</em> row for the requested key (safe: only touches rows
 *       whose {@code expire_time &le; now}, never affecting valid locks held by other nodes).</li>
 *   <li>Insert a new lock row.  Because {@code lock_key} is the primary key, a
 *       concurrent insert by another node will cause a {@link DataIntegrityViolationException},
 *       which this method catches and converts to {@code false}.</li>
 * </ol>
 * This eliminates the TOCTOU window present in the original three-step
 * (find → delete → insert) approach.
 *
 * <h3>Ownership verification</h3>
 * Both {@code owner} (node ID) and {@code lockValue} (per-acquisition UUID) must match
 * to release a lock, preventing one node from accidentally releasing another node's lock.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

  private final DistributedLockRepository lockRepository;
  private final JobProperties properties;

  /**
   * Attempts to acquire a lock with the default timeout from {@link JobProperties}.
   *
   * @param lockKey unique lock identifier
   * @param owner   identifier of the requesting node (e.g. hostname + UUID)
   * @return the lock value (UUID) to pass to {@link #unlock} if successful, or {@code null} if the
   * lock is already held by another node
   */
  public String tryLock(String lockKey, String owner) {
    return tryLock(lockKey, owner, properties.getLockTimeoutSeconds());
  }

  /**
   * Attempts to acquire a lock with a custom timeout.
   *
   * @param lockKey        unique lock identifier
   * @param owner          identifier of the requesting node
   * @param timeoutSeconds lock TTL in seconds
   * @return the lock value (UUID) to pass to {@link #unlock} if successful, or {@code null} if the
   * lock is already held by another node
   */
  @Transactional
  public String tryLock(String lockKey, String owner, int timeoutSeconds) {
    try {
      LocalDateTime now = LocalDateTime.now();

      // Step 1: remove any expired lock for this key (safe, atomic delete).
      lockRepository.deleteExpiredLockByKey(lockKey, now);

      // Step 2: attempt to insert a new lock row.
      // If another node holds a valid lock, the PK uniqueness constraint fires
      // and DataIntegrityViolationException is thrown.
      String lockValue = UUID.randomUUID().toString();
      DistributedLock lock = new DistributedLock();
      lock.setLockKey(lockKey);
      lock.setLockValue(lockValue);
      lock.setOwner(owner);
      lock.setAcquireTime(now);
      lock.setExpireTime(now.plusSeconds(timeoutSeconds));

      lockRepository.saveAndFlush(lock);
      log.debug("Lock acquired: key={} owner={}", lockKey, owner);
      return lockValue;

    } catch (DataIntegrityViolationException e) {
      // Another node holds a valid lock.
      log.debug("Lock contention: key={} is already held", lockKey);
      return null;
    } catch (Exception e) {
      log.error("Failed to acquire lock: key={}", lockKey, e);
      return null;
    }
  }

  /**
   * Releases a lock, verifying both {@code owner} and {@code lockValue} to prevent accidental or
   * spoofed releases.
   *
   * @param lockKey   lock to release
   * @param owner     must match the owner stored in the lock row
   * @param lockValue must match the value returned by {@link #tryLock}
   */
  @Transactional
  public void unlock(String lockKey, String owner, String lockValue) {
    try {
      Optional<DistributedLock> optLock = lockRepository.findById(lockKey);
      if (optLock.isEmpty()) {
        log.debug("Lock not found on release (already expired?): key={}", lockKey);
        return;
      }
      DistributedLock lock = optLock.get();
      if (!owner.equals(lock.getOwner()) || !lockValue.equals(lock.getLockValue())) {
        log.warn("Lock release rejected: key={} — owner/value mismatch "
                + "(expected owner={} value={}, got owner={} value={})",
            lockKey, lock.getOwner(), lock.getLockValue(), owner, lockValue);
        return;
      }
      lockRepository.deleteById(lockKey);
      log.debug("Lock released: key={} owner={}", lockKey, owner);
    } catch (Exception e) {
      log.error("Failed to release lock: key={}", lockKey, e);
    }
  }

  /**
   * Extends the expiry of a lock already held by the caller.
   *
   * @param lockKey        lock to renew
   * @param owner          must match the stored owner
   * @param lockValue      must match the stored lock value
   * @param timeoutSeconds new TTL measured from now
   * @return {@code true} if the lock was successfully renewed
   */
  @Transactional
  public boolean renewLock(String lockKey, String owner, String lockValue, int timeoutSeconds) {
    try {
      Optional<DistributedLock> optLock = lockRepository.findById(lockKey);
      if (optLock.isEmpty()) {
        return false;
      }
      DistributedLock lock = optLock.get();
      if (!owner.equals(lock.getOwner()) || !lockValue.equals(lock.getLockValue())) {
        return false;
      }
      lock.setExpireTime(LocalDateTime.now().plusSeconds(timeoutSeconds));
      lockRepository.save(lock);
      return true;
    } catch (Exception e) {
      log.error("Failed to renew lock: key={}", lockKey, e);
      return false;
    }
  }
}
