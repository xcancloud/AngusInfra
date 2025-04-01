package cloud.xcan.angus.security.authentication.dao;

import cloud.xcan.angus.security.model.CustomOAuth2User;
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
public class CaffeineCacheBasedUserCache {

  private final Cache<String, CustomOAuth2User> cache;

  public CaffeineCacheBasedUserCache() {
    this.cache = Caffeine.newBuilder()
        .maximumSize(2048)
        .expireAfterWrite(30, java.util.concurrent.TimeUnit.MINUTES)
        .expireAfterAccess(5, java.util.concurrent.TimeUnit.MINUTES)
        .build();
  }

  public CustomOAuth2User getUserFromCache(String compositeAccount) {
    CustomOAuth2User element = this.cache.getIfPresent(compositeAccount);
    log.debug("Cache hit: " + (element != null) + "; compositeAccount: " + compositeAccount);
    return element;
  }

  public void putUserInCache(String compositeAccount, CustomOAuth2User user) {
    log.debug("Cache put: " + compositeAccount);
    this.cache.put(compositeAccount, user);
  }

  public void removeUserFromCache(String compositeAccount) {
    log.debug("Cache remove: " + compositeAccount);
    this.cache.invalidate(compositeAccount);
  }

}
