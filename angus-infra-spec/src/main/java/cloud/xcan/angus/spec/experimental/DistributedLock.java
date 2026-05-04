package cloud.xcan.angus.spec.experimental;


import java.util.concurrent.TimeUnit;

/**
 * Distributed Lock Reliability - To ensure the availability of a distributed lock, the
 * implementation must satisfy the following four conditions:
 *
 * <pre>
 * 1. Mutual Exclusion: At any given time, only one client can hold the lock.
 * 2. Deadlock-Free: Even if a client crashes while holding the lock and does not actively release it, other clients can still acquire the lock.
 * 3. Fault Tolerance: As long as the majority of Redis nodes are running, clients can acquire and release locks.
 * 4. Unlock by the Locker: The lock must be released by the same client that acquired it. A client cannot release a lock held by another client.
 * </pre>
 */
public interface DistributedLock {

  /**
   * Attempts to acquire a distributed lock in an atomic operation.
   *
   * @param lockKey The lock key.
   * @param reqId   The identifier of the lock holder.
   * @param expire  The expiration time in milliseconds (should be set reasonably based on the
   *                business logic to prevent the lock from being automatically released before the
   *                business logic completes).
   * @return Whether the lock was successfully acquired.
   */
  boolean tryLock(String lockKey, String reqId, long expire, TimeUnit timeUnit);

  /**
   * Releases a distributed lock.
   *
   * @param lockKey   The lock key.
   * @param requestId The identifier of the lock holder.
   * @return Whether the lock was successfully released.
   */
  boolean releaseLock(String lockKey, String requestId);

  /**
   * Retrieves the value of the Redis lock.
   */
  String get(String lockKey);

}
