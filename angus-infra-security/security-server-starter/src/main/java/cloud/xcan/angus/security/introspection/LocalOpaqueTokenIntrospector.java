package cloud.xcan.angus.security.introspection;

import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.AUTHORITY_SCOPE_PREFIX;

import cloud.xcan.angus.security.authentication.CustomOAuth2TokenIntrospectionAuthenticationProvider;
import cloud.xcan.angus.security.model.SecurityConstant;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenIntrospection;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.Assert;

/**
 * In-process {@link OpaqueTokenIntrospector} for apps that co-locate the authorization server and
 * resource server (GM). Avoids a self-HTTP call to {@code /oauth2/introspect}.
 */
public class LocalOpaqueTokenIntrospector
    implements OpaqueTokenIntrospector, IntrospectionCacheInvalidator {

  private final CustomOAuth2TokenIntrospectionAuthenticationProvider introspectionProvider;

  private Cache<String, OAuth2AuthenticatedPrincipal> resultCache;

  public LocalOpaqueTokenIntrospector(
      CustomOAuth2TokenIntrospectionAuthenticationProvider introspectionProvider) {
    Assert.notNull(introspectionProvider, "introspectionProvider cannot be null");
    this.introspectionProvider = introspectionProvider;
  }

  public void setResultCache(boolean enabled, int ttlSeconds, long maximumSize) {
    if (!enabled || ttlSeconds <= 0 || maximumSize <= 0) {
      this.resultCache = null;
      return;
    }
    this.resultCache = Caffeine.newBuilder()
        .maximumSize(maximumSize)
        .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
        .build();
  }

  @Override
  public void invalidateToken(String token) {
    Cache<String, OAuth2AuthenticatedPrincipal> cache = this.resultCache;
    if (cache == null || token == null || token.isEmpty()) {
      return;
    }
    cache.invalidate(sha256Hex(token));
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {
    String cacheKey = null;
    Cache<String, OAuth2AuthenticatedPrincipal> cache = this.resultCache;
    if (cache != null) {
      cacheKey = sha256Hex(token);
      OAuth2AuthenticatedPrincipal cached = cache.getIfPresent(cacheKey);
      if (cached != null) {
        if (isPrincipalExpired(cached)) {
          cache.invalidate(cacheKey);
        } else {
          return cached;
        }
      }
    }

    OAuth2TokenIntrospection claims = introspectionProvider.introspectLocally(token);
    if (claims == null || !claims.isActive()) {
      throw new BadOpaqueTokenException("Provided token isn't active");
    }

    OAuth2AuthenticatedPrincipal principal = toPrincipal(claims.getClaims());
    if (cache != null && cacheKey != null) {
      cache.put(cacheKey, principal);
    }
    return principal;
  }

  private static OAuth2AuthenticatedPrincipal toPrincipal(Map<String, Object> claims) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    Object scope = claims.get(OAuth2TokenIntrospectionClaimNames.SCOPE);
    if (scope instanceof Collection<?> scopes) {
      for (Object s : scopes) {
        if (s != null) {
          authorities.add(new SimpleGrantedAuthority(AUTHORITY_SCOPE_PREFIX + s));
        }
      }
    }
    Object permissions = claims.get(SecurityConstant.INTROSPECTION_CLAIM_NAMES_PERMISSION);
    if (permissions instanceof Collection<?> perms) {
      for (Object p : perms) {
        if (p != null) {
          authorities.add(new SimpleGrantedAuthority(p.toString()));
        }
      }
    } else if (permissions instanceof String permCsv) {
      for (String p : permCsv.split(" ")) {
        if (!p.isBlank()) {
          authorities.add(new SimpleGrantedAuthority(p));
        }
      }
    }
    return new OAuth2IntrospectionAuthenticatedPrincipal(claims, authorities);
  }

  private static boolean isPrincipalExpired(OAuth2AuthenticatedPrincipal principal) {
    Object exp = principal.getAttribute(OAuth2TokenIntrospectionClaimNames.EXP);
    if (exp instanceof Instant instant) {
      return Instant.now().isAfter(instant);
    }
    return false;
  }

  private static String sha256Hex(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      return Integer.toHexString(token.hashCode());
    }
  }
}
