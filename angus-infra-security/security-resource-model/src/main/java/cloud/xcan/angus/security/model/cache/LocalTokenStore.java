package cloud.xcan.angus.security.model.cache;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory (local) token store using volatile fields for thread-safe single-instance caching.
 *
 * <p>This implementation is suitable for single-instance deployments where tokens only need to be
 * cached within the same JVM process. For multi-instance deployments, use
 * {@code DistributedTokenStore} instead.</p>
 *
 * @author Framework Team
 * @version 1.0
 * @since 2025-03-22
 */
@Slf4j
public class LocalTokenStore implements TokenStore {

  private volatile String cachedToken;
  private volatile long cachedTokenTime = 0;
  private volatile long ttlMillis = 0;

  @Override
  public void store(String key, String token, long ttlSeconds) {
    this.cachedToken = token;
    this.cachedTokenTime = System.currentTimeMillis();
    this.ttlMillis = ttlSeconds * 1000;
    log.debug("Token stored locally: key={}, ttl={}s", key, ttlSeconds);
  }

  @Override
  public Optional<String> retrieve(String key) {
    if (cachedToken == null) {
      return Optional.empty();
    }
    long age = System.currentTimeMillis() - cachedTokenTime;
    if (ttlMillis > 0 && age >= ttlMillis) {
      log.debug("Local token expired: key={}, age={}ms, ttl={}ms", key, age, ttlMillis);
      return Optional.empty();
    }
    return Optional.of(cachedToken);
  }

  @Override
  public void remove(String key) {
    cachedToken = null;
    cachedTokenTime = 0;
    log.debug("Token removed from local store: key={}", key);
  }

  @Override
  public boolean exists(String key) {
    return retrieve(key).isPresent();
  }
}
