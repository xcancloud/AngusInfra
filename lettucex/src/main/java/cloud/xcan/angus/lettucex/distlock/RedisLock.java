package cloud.xcan.angus.lettucex.distlock;

import static cloud.xcan.angus.spec.SpecConstant.UTF8;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.experimental.DistributedLock;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

/**
 * <p>
 * Redis-based distributed lock implementation that provides mutual exclusion
 * across multiple application instances. This implementation uses Redis SET command
 * with NX (Not eXists) and EX (EXpire) options to ensure atomicity and prevent deadlocks.
 * </p>
 * 
 * <p>
 * Key features:
 * - Atomic lock acquisition using Redis SET NX EX
 * - Safe lock release using Lua script to prevent race conditions
 * - Configurable lock expiration to prevent deadlocks
 * - Request ID validation to ensure only lock owner can release
 * - Thread-safe operations
 * </p>
 * 
 * <p>
 * Usage example:
 * <pre>
 * RedisLock lock = new RedisLock(stringRedisTemplate);
 * String requestId = UUID.randomUUID().toString();
 * 
 * if (lock.tryLock("myLock", requestId, 30, TimeUnit.SECONDS)) {
 *     try {
 *         // Critical section
 *     } finally {
 *         lock.releaseLock("myLock", requestId);
 *     }
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * Thread Safety: This class is thread-safe and can be used concurrently.
 * </p>
 */
@Slf4j
public class RedisLock implements DistributedLock {

  /**
   * Lua script for atomic lock release operation.
   * This script ensures that only the lock owner (verified by request ID) can release the lock,
   * preventing accidental release by other processes.
   * 
   * Script logic:
   * 1. Get the current value of the lock key
   * 2. Compare it with the provided request ID
   * 3. If they match, delete the key (release lock)
   * 4. Return 1 if released, 0 if not (key doesn't exist or request ID mismatch)
   */
  private static final String UNLOCK_LUA_SCRIPT =
      "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
      "then " +
      "    return redis.call(\"del\",KEYS[1]) " +
      "else " +
      "    return 0 " +
      "end ";

  /**
   * Redis template for executing Redis commands.
   * Uses StringRedisTemplate for better performance with string operations.
   */
  private final StringRedisTemplate redisTemplate;

  /**
   * <p>
   * Constructor for creating a Redis distributed lock instance.
   * </p>
   *
   * @param redisTemplate the Redis template for executing commands
   * @throws IllegalArgumentException if redisTemplate is null
   */
  public RedisLock(StringRedisTemplate redisTemplate) {
    this.redisTemplate = Objects.requireNonNull(redisTemplate, 
        "StringRedisTemplate cannot be null");
  }

  /**
   * <p>
   * Attempts to acquire a distributed lock with the specified parameters.
   * Uses Redis SET command with NX (not exists) and EX (expiration) options
   * to ensure atomic lock acquisition with automatic expiration.
   * </p>
   * 
   * <p>
   * The lock will automatically expire after the specified time to prevent deadlocks
   * in case the lock holder crashes or fails to release the lock properly.
   * </p>
   *
   * @param lockKey the unique key identifying the lock resource
   * @param reqId unique request identifier to prevent lock hijacking
   * @param expire the lock expiration time
   * @param timeUnit the time unit for the expiration
   * @return true if the lock was successfully acquired, false otherwise
   */
  @Override
  public boolean tryLock(String lockKey, String reqId, long expire, TimeUnit timeUnit) {
    // Validate input parameters
    if (lockKey == null || lockKey.trim().isEmpty()) {
      log.warn("Attempted to acquire lock with null or empty lockKey");
      return false;
    }
    
    if (reqId == null || reqId.trim().isEmpty()) {
      log.warn("Attempted to acquire lock with null or empty reqId for key: {}", lockKey);
      return false;
    }
    
    if (expire <= 0) {
      log.warn("Attempted to acquire lock with non-positive expiration for key: {}", lockKey);
      return false;
    }

    try {
      // Use Redis SET command with NX (not exists) and EX (expiration) options
      // This ensures atomicity: either the key is set with expiration, or the operation fails
      RedisCallback<Boolean> callback = (connection) -> {
        byte[] keyBytes = lockKey.getBytes(UTF8);
        byte[] valueBytes = reqId.getBytes(UTF8);
        long expireSeconds = timeUnit.toSeconds(expire);
        
        return connection.set(keyBytes, valueBytes, 
            Expiration.seconds(expireSeconds), 
            RedisStringCommands.SetOption.SET_IF_ABSENT);
      };
      
      Boolean result = redisTemplate.execute(callback);
      boolean lockAcquired = nonNull(result) && result;
      
      if (lockAcquired) {
        log.debug("Successfully acquired lock for key: {}, reqId: {}, expire: {} {}", 
            lockKey, reqId, expire, timeUnit);
      } else {
        log.debug("Failed to acquire lock for key: {}, reqId: {} (lock already exists)", 
            lockKey, reqId);
      }
      
      return lockAcquired;
      
    } catch (Exception e) {
      log.error("Error occurred while trying to acquire lock for key: {}, reqId: {}", 
          lockKey, reqId, e);
      return false;
    }
  }

  /**
   * <p>
   * Releases a distributed lock using a Lua script to ensure atomicity.
   * The script verifies that the request ID matches the stored value before
   * releasing the lock, preventing accidental release by other processes.
   * </p>
   * 
   * <p>
   * This method is idempotent - calling it multiple times with the same parameters
   * will not cause errors, though only the first call will actually release the lock.
   * </p>
   *
   * @param lockKey the unique key identifying the lock resource
   * @param requestId the request identifier used when acquiring the lock
   * @return true if the lock was successfully released, false if the lock doesn't exist
   *         or the request ID doesn't match
   */
  @Override
  public boolean releaseLock(String lockKey, String requestId) {
    // Validate input parameters
    if (lockKey == null || lockKey.trim().isEmpty()) {
      log.warn("Attempted to release lock with null or empty lockKey");
      return false;
    }
    
    if (requestId == null || requestId.trim().isEmpty()) {
      log.warn("Attempted to release lock with null or empty requestId for key: {}", lockKey);
      return false;
    }

    try {
      // Use Lua script to ensure atomic check-and-delete operation
      RedisCallback<Boolean> callback = (connection) -> {
        byte[] scriptBytes = UNLOCK_LUA_SCRIPT.getBytes(UTF8);
        byte[] keyBytes = lockKey.getBytes(UTF8);
        byte[] valueBytes = requestId.getBytes(UTF8);
        
        // Execute Lua script with the lock key and request ID
        return connection.eval(scriptBytes, ReturnType.BOOLEAN, 1, keyBytes, valueBytes);
      };
      
      Boolean result = redisTemplate.execute(callback);
      boolean lockReleased = nonNull(result) && result;
      
      if (lockReleased) {
        log.debug("Successfully released lock for key: {}, requestId: {}", lockKey, requestId);
      } else {
        log.debug("Failed to release lock for key: {}, requestId: {} (lock not found or ID mismatch)", 
            lockKey, requestId);
      }
      
      return lockReleased;
      
    } catch (Exception e) {
      log.error("Error occurred while trying to release lock for key: {}, requestId: {}", 
          lockKey, requestId, e);
      return false;
    }
  }

  /**
   * <p>
   * Retrieves the current value (request ID) stored for the specified lock key.
   * This method can be used to check if a lock exists and who owns it.
   * </p>
   * 
   * <p>
   * Note: This method should be used carefully as the lock state might change
   * between the time this method returns and when the result is used.
   * </p>
   *
   * @param lockKey the unique key identifying the lock resource
   * @return the request ID of the current lock holder, or null if the lock doesn't exist
   */
  @Override
  public String get(String lockKey) {
    // Validate input parameter
    if (lockKey == null || lockKey.trim().isEmpty()) {
      log.warn("Attempted to get lock value with null or empty lockKey");
      return null;
    }

    try {
      RedisCallback<String> callback = (connection) -> {
        byte[] keyBytes = lockKey.getBytes(UTF8);
        byte[] valueBytes = connection.get(keyBytes);
        
        return valueBytes != null ? new String(valueBytes, UTF8) : null;
      };
      
      String value = redisTemplate.execute(callback);
      
      if (log.isDebugEnabled()) {
        log.debug("Retrieved lock value for key: {}, value: {}", lockKey, 
            value != null ? value : "null");
      }
      
      return value;
      
    } catch (Exception e) {
      log.error("Error occurred while trying to get lock value for key: {}", lockKey, e);
      return null;
    }
  }

  /**
   * <p>
   * Checks if a lock with the specified key currently exists.
   * This is a convenience method that checks for lock existence without
   * retrieving the actual request ID value.
   * </p>
   *
   * @param lockKey the unique key identifying the lock resource
   * @return true if the lock exists, false otherwise
   */
  public boolean exists(String lockKey) {
    if (lockKey == null || lockKey.trim().isEmpty()) {
      return false;
    }

    try {
      Boolean exists = redisTemplate.hasKey(lockKey);
      return nonNull(exists) && exists;
    } catch (Exception e) {
      log.error("Error occurred while checking lock existence for key: {}", lockKey, e);
      return false;
    }
  }

  /**
   * <p>
   * Retrieves the remaining time-to-live (TTL) for the specified lock key.
   * This can be useful for monitoring lock expiration times.
   * </p>
   *
   * @param lockKey the unique key identifying the lock resource
   * @param timeUnit the desired time unit for the result
   * @return the remaining TTL, or -1 if the key doesn't exist, -2 if the key exists but has no TTL
   */
  public long getTtl(String lockKey, TimeUnit timeUnit) {
    if (lockKey == null || lockKey.trim().isEmpty()) {
      return -1;
    }

    try {
      Long ttlSeconds = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
      if (ttlSeconds == null) {
        return -1;
      }
      
      return timeUnit.convert(ttlSeconds, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error("Error occurred while getting TTL for lock key: {}", lockKey, e);
      return -1;
    }
  }

  /**
   * <p>
   * Extends the expiration time of an existing lock if the request ID matches.
   * This can be useful for long-running operations that need to extend their lock time.
   * </p>
   * 
   * <p>
   * Note: This operation is not atomic and should be used carefully.
   * Consider implementing a Lua script for atomic lock extension if needed.
   * </p>
   *
   * @param lockKey the unique key identifying the lock resource
   * @param requestId the request identifier used when acquiring the lock
   * @param expire the new expiration time
   * @param timeUnit the time unit for the expiration
   * @return true if the lock was successfully extended, false otherwise
   */
  public boolean extendLock(String lockKey, String requestId, long expire, TimeUnit timeUnit) {
    if (lockKey == null || lockKey.trim().isEmpty() || 
        requestId == null || requestId.trim().isEmpty() || expire <= 0) {
      return false;
    }

    try {
      // Check if the current lock holder matches the request ID
      String currentHolder = get(lockKey);
      if (requestId.equals(currentHolder)) {
        // Extend the expiration time
        Boolean result = redisTemplate.expire(lockKey, expire, timeUnit);
        boolean extended = nonNull(result) && result;
        
        if (extended) {
          log.debug("Successfully extended lock for key: {}, requestId: {}, expire: {} {}", 
              lockKey, requestId, expire, timeUnit);
        }
        
        return extended;
      } else {
        log.debug("Cannot extend lock for key: {}, requestId: {} (ID mismatch or lock not found)", 
            lockKey, requestId);
        return false;
      }
    } catch (Exception e) {
      log.error("Error occurred while extending lock for key: {}, requestId: {}", 
          lockKey, requestId, e);
      return false;
    }
  }
}
