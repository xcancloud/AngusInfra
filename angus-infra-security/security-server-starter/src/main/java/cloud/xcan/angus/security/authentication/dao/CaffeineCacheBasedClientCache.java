package cloud.xcan.angus.security.authentication.dao;

import cloud.xcan.angus.security.client.CustomOAuth2RegisteredClient;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;

/**
 * Caches {@link UserDetails} instances in a Caffeine defined {@link Cache}.
 *
 * @see SpringCacheBasedUserCache
 */
@Slf4j
public class CaffeineCacheBasedClientCache {

  private final Cache<String, CustomOAuth2RegisteredClient> cache;

  public CaffeineCacheBasedClientCache() {
    this.cache = Caffeine.newBuilder()
        .maximumSize(526)
        .expireAfterWrite(30, java.util.concurrent.TimeUnit.MINUTES)
        .expireAfterAccess(5, java.util.concurrent.TimeUnit.MINUTES)
        .build();
  }

  public CustomOAuth2RegisteredClient getClientFromCache(String clientId) {
    CustomOAuth2RegisteredClient element = this.cache.getIfPresent(clientId);
    log.debug("Cache hit: " + (element != null) + "; clientId: " + clientId);
    return element;
  }

  public void putClientInCache(String clientId, CustomOAuth2RegisteredClient user) {
    log.debug("Cache put: " + clientId);
    this.cache.put(clientId, user);
  }

  public void removeClientFromCache(String clientId) {
    log.debug("Cache remove: " + clientId);
    this.cache.invalidate(clientId);
  }

}
