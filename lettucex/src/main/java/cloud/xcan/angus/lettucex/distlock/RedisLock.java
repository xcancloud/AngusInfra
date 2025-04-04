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
 * Redis Distributed Lock Implements.
 */
@Slf4j
public class RedisLock implements DistributedLock {

  private static final String UNLOCK_LUA =
      "if redis.call(\"get\",KEYS[1]) == ARGV[1] "
          + "then "
          + "    return redis.call(\"del\",KEYS[1]) "
          + "else "
          + "    return 0 "
          + "end ";

  private final StringRedisTemplate redisTemplate;

  public RedisLock(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean tryLock(String lockKey, String reqId, long expire, TimeUnit timeUnit) {
    try {
      RedisCallback<Boolean> callback = (connection) -> connection.set(lockKey.getBytes(UTF8),
          reqId.getBytes(UTF8), Expiration.seconds(timeUnit.toSeconds(expire)),
          RedisStringCommands.SetOption.SET_IF_ABSENT);
      Object res = redisTemplate.execute(callback);
      return nonNull(res) && (boolean) res;
    } catch (Exception e) {
      log.warn("Redis lock error, cause: {}", e.getMessage());
    }
    return false;
  }

  @Override
  public boolean releaseLock(String lockKey, String requestId) {
    RedisCallback<Boolean> callback = (connection) -> connection
        .eval(UNLOCK_LUA.getBytes(), ReturnType.BOOLEAN, 1, lockKey.getBytes(UTF8),
            requestId.getBytes(UTF8));
    Object res = redisTemplate.execute(callback);
    return nonNull(res) && (boolean) res;
  }

  @Override
  public String get(String lockKey) {
    try {
      RedisCallback<String> callback = (connection) -> new String(
          Objects.requireNonNull(connection.get(lockKey.getBytes())), UTF8);
      String value = redisTemplate.execute(callback);
      return nonNull(value) ? value : null;
    } catch (Exception e) {
      log.warn("Redis lock error, cause: {}", e.getMessage());
    }
    return null;
  }

}
