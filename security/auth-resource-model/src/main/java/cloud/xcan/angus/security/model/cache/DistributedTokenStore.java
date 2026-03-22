package cloud.xcan.angus.security.model.cache;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Distributed token store backed by an {@code IDistributedCache} implementation.
 *
 * <p>This implementation is suitable for multi-instance deployments where tokens must be shared
 * across all service instances. It delegates to the {@code IDistributedCache} interface, which
 * may be backed by Redis, database-backed hybrid cache, or any other distributed cache.</p>
 *
 * <p>Uses the {@code IDistributedCache} from {@code xcan-angusinfra.cache} module.</p>
 *
 * @author Framework Team
 * @version 1.0
 * @since 2025-03-22
 */
@Slf4j
public class DistributedTokenStore implements TokenStore {

  /**
   * We accept Object to avoid a hard compile-time dependency on the cache module.
   * At runtime the auto-configurer passes the real {@code IDistributedCache} bean.
   */
  private final Object distributedCache;

  public DistributedTokenStore(Object distributedCache) {
    this.distributedCache = distributedCache;
    log.info("DistributedTokenStore initialized with cache: {}",
        distributedCache.getClass().getSimpleName());
  }

  @Override
  public void store(String key, String token, long ttlSeconds) {
    try {
      invoke("set", new Class<?>[]{String.class, String.class, Long.class},
          key, token, ttlSeconds);
      log.debug("Token stored in distributed cache: key={}, ttl={}s", key, ttlSeconds);
    } catch (Exception e) {
      log.error("Failed to store token in distributed cache: key={}, error={}",
          key, e.getMessage(), e);
      throw new RuntimeException("Distributed token store failure", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<String> retrieve(String key) {
    try {
      Object result = invoke("get", new Class<?>[]{String.class}, key);
      if (result instanceof Optional) {
        Optional<String> opt = (Optional<String>) result;
        if (opt.isPresent()) {
          log.debug("Token retrieved from distributed cache: key={}", key);
        }
        return opt;
      }
      return Optional.empty();
    } catch (Exception e) {
      log.warn("Failed to retrieve token from distributed cache: key={}, error={}",
          key, e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Override
  public void remove(String key) {
    try {
      invoke("delete", new Class<?>[]{String.class}, key);
      log.debug("Token removed from distributed cache: key={}", key);
    } catch (Exception e) {
      log.warn("Failed to remove token from distributed cache: key={}, error={}",
          key, e.getMessage(), e);
    }
  }

  @Override
  public boolean exists(String key) {
    return retrieve(key).isPresent();
  }

  private Object invoke(String methodName, Class<?>[] paramTypes, Object... args) {
    try {
      java.lang.reflect.Method method = distributedCache.getClass()
          .getMethod(methodName, paramTypes);
      return method.invoke(distributedCache, args);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke " + methodName + " on distributed cache", e);
    }
  }
}
