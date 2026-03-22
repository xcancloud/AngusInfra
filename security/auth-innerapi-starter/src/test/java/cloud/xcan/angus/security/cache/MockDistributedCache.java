package cloud.xcan.angus.security.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple mock that matches IDistributedCache method signatures. DistributedTokenStore uses
 * reflection, so this works without the cache module. Must be a public top-level class so
 * reflection from DistributedTokenStore can access the methods.
 */
public class MockDistributedCache {

  final Map<String, String> data = new HashMap<>();

  public void set(String key, String value, Long ttlSeconds) {
    data.put(key, value);
  }

  public Optional<String> get(String key) {
    return Optional.ofNullable(data.get(key));
  }

  public void delete(String key) {
    data.remove(key);
  }
}
