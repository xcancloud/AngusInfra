package cloud.xcan.angus.security.authentication.dao;

import static cloud.xcan.angus.security.authentication.dao.AbstractUserDetailsAuthenticationProvider.COMPOSITE_ACCOUNT_SEPARATOR;
import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.security.model.CustomOAuth2User;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
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

  public void putUserInCache(@Nullable String userId, String account, CustomOAuth2User user) {
    String compositeAccount = getCompositeAccountCacheKey(userId, account);
    log.debug("Cache put: " + compositeAccount);
    this.cache.put(compositeAccount, user);
  }

  public void removeUserFromCache(String compositeAccount) {
    log.debug("Cache remove: " + compositeAccount);
    this.cache.invalidate(compositeAccount);
  }

  public void removeUserFromCache(@Nullable String userId, String account) {
    String compositeAccount = getCompositeAccountCacheKey(userId, account);
    log.debug("Cache remove: " + compositeAccount);
    this.cache.invalidate(compositeAccount);
  }

  public static String getCompositeAccountCacheKey(String userId, String account) {
   return isNotEmpty(userId)
       ? format("%s%s%s", userId, COMPOSITE_ACCOUNT_SEPARATOR, account) : account;
  }

}
