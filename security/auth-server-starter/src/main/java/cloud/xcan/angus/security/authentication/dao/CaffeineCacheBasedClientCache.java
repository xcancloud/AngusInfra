package cloud.xcan.angus.security.authentication.dao;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * Caches {@link UserDetails} instances in a Caffeine defined {@link Cache}.
 *
 * @see SpringCacheBasedUserCache
 */
@Slf4j
public class CaffeineCacheBasedClientCache {

  private final Cache<String, RegisteredClient> cache;

  public CaffeineCacheBasedClientCache() {
    this.cache = Caffeine.newBuilder()
        .maximumSize(526)
        .expireAfterWrite(30, java.util.concurrent.TimeUnit.MINUTES)
        .expireAfterAccess(5, java.util.concurrent.TimeUnit.MINUTES)
        .build();
  }

  public RegisteredClient getClientFromCache(String clientId) {
    RegisteredClient element = this.cache.getIfPresent(clientId);
    log.debug("Cache hit: " + (element != null) + "; clientId: " + clientId);
    return element;
  }

  public void putClientInCache(String clientId, RegisteredClient user) {
    log.debug("Cache put: " + clientId);
    this.cache.put(clientId, user);
  }

  public void removeClientFromCache(String clientId) {
    log.debug("Cache remove: " + clientId);
    this.cache.invalidate(clientId);
  }

}
