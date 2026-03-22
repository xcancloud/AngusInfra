package cloud.xcan.angus.security.cache;

import java.util.Optional;

/**
 * Mock that throws exceptions on every operation, for error-handling tests. Must be a public
 * top-level class so reflection from DistributedTokenStore can access the methods.
 */
public class FailingDistributedCache {

  public void set(String key, String value, Long ttlSeconds) {
    throw new RuntimeException("Cache unavailable");
  }

  public Optional<String> get(String key) {
    throw new RuntimeException("Cache unavailable");
  }

  public void delete(String key) {
    throw new RuntimeException("Cache unavailable");
  }
}
