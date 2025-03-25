package cloud.xcan.angus.lettucex.distlock;


import static cloud.xcan.angus.spec.SpecConstant.UTF8;
import static java.util.Objects.nonNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

/**
 * Lock Reliability - To ensure the availability of a distributed lock, the implementation must
 * satisfy the following four conditions:
 *
 * <pre>
 * 1. Mutual Exclusion: At any given time, only one client can hold the lock.
 * 2. Deadlock-Free: Even if a client crashes while holding the lock and does not actively release it, other clients can still acquire the lock.
 * 3. Fault Tolerance: As long as the majority of Redis nodes are running, clients can acquire and release locks.
 * 4. Unlock by the Locker: The lock must be released by the same client that acquired it. A client cannot release a lock held by another client.
 * </pre>
 */
@Slf4j
public class RedisLock {

  private static final String UNLOCK_LUA =
      "if redis.call(\"get\",KEYS[1]) == ARGV[1] "
          + "then "
          + "    return redis.call(\"del\",KEYS[1]) "
          + "else "
          + "    return 0 "
          + "end ";

  private RedisTemplate redisTemplate;

  public RedisLock(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

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
  public boolean tryLock(String lockKey, String reqId, long expire, TimeUnit timeUnit) {
    try {
      RedisCallback<Boolean> callback = (connection) -> connection.set(lockKey.getBytes(UTF8),
          reqId.getBytes(UTF8), Expiration.seconds(timeUnit.toSeconds(expire)),
          RedisStringCommands.SetOption.SET_IF_ABSENT);
      Object res = redisTemplate.execute(callback);
      return nonNull(res) && (boolean) res;
    } catch (Exception e) {
      log.warn("redis lock error, cause: {}", e.getMessage());
    }
    return false;
  }

  /**
   * Releases a distributed lock.
   *
   * @param lockKey   The lock key.
   * @param requestId The identifier of the lock holder.
   * @return Whether the lock was successfully released.
   */
  public boolean releaseLock(String lockKey, String requestId) {
    RedisCallback<Boolean> callback = (connection) -> connection
        .eval(UNLOCK_LUA.getBytes(), ReturnType.BOOLEAN, 1, lockKey.getBytes(UTF8),
            requestId.getBytes(UTF8));
    Object res = redisTemplate.execute(callback);
    return nonNull(res) && (boolean) res;
  }

  /**
   * Retrieves the value of the Redis lock.
   */
  public String get(String lockKey) {
    try {
      RedisCallback<String> callback = (connection) -> new String(
          Objects.requireNonNull(connection.get(lockKey.getBytes())), UTF8);
      Object value = redisTemplate.execute(callback);
      return nonNull(value) ? (String) value : null;
    } catch (Exception e) {
      log.warn("redis lock error, cause: {}", e.getMessage());
    }
    return null;
  }

}
