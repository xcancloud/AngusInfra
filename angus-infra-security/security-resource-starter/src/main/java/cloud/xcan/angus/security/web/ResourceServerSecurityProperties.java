package cloud.xcan.angus.security.web;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * General resource-server security knobs that are not specific to the artifact-protocol Basic
 * bridge (see {@link BasicAuthBridgeProperties}).
 *
 * <p><b>{@code allowUriQueryToken}.</b> When {@code true}, the {@code BearerTokenResolver}
 * additionally accepts the access token via the {@code ?access_token=} URL query parameter
 * (Spring's {@code DefaultBearerTokenResolver#setAllowUriQueryParameter(true)}). RFC 6750 §2.3
 * discourages this carrier because the token then leaks into access logs, reverse-proxy logs and
 * {@code Referer} headers. The default is therefore {@code false} (the RFC-recommended secure
 * default); services that still rely on query-parameter tokens (e.g. legacy browser download /
 * preview links) opt back in by setting it to {@code true}.</p>
 *
 * <p><b>{@code allowedClientIds}.</b> Portal client boundary for this resource server. When
 * non-empty, tokens issued to platform portals ({@code xcan_tp} / {@code xcan_op}) are rejected
 * unless the token's {@code client_id} is listed here. Non-portal service clients
 * ({@code client_credentials}) are never gated by this list. Empty (default) disables the check
 * for backward compatibility — production apps should always set it to match
 * {@code gm_application.client_id}.</p>
 */
@ConfigurationProperties(prefix = "angus.security.resource")
public class ResourceServerSecurityProperties {

  /**
   * Whether the access token may be supplied via the {@code ?access_token=} URL query parameter.
   * Default {@code false} per the RFC 6750 §2.3 recommendation (keeps tokens out of URLs / access
   * logs). Set to {@code true} to restore the legacy query-parameter carrier.
   */
  private boolean allowUriQueryToken = false;

  /**
   * Portal clients allowed to call this service's authenticated APIs (typically {@code xcan_tp}
   * and/or {@code xcan_op}). Empty disables cross-client enforcement.
   */
  private List<String> allowedClientIds = new ArrayList<>();

  /**
   * Whether to cache successful opaque-token introspection results in-process. Default
   * {@code true}; set {@code false} to force a remote introspect on every request.
   */
  private boolean introspectCacheEnabled = true;

  /**
   * TTL for introspection result cache entries (seconds). Default {@code 60}. Permission /
   * revocation changes may take up to this long to be visible on resource servers.
   */
  private int introspectCacheTtlSeconds = 60;

  /**
   * Maximum number of cached introspection principals. Default {@code 10000}.
   */
  private long introspectCacheMaximumSize = 10_000L;

  public boolean isAllowUriQueryToken() {
    return allowUriQueryToken;
  }

  public void setAllowUriQueryToken(boolean allowUriQueryToken) {
    this.allowUriQueryToken = allowUriQueryToken;
  }

  public List<String> getAllowedClientIds() {
    return allowedClientIds;
  }

  public void setAllowedClientIds(List<String> allowedClientIds) {
    this.allowedClientIds = allowedClientIds != null ? allowedClientIds : new ArrayList<>();
  }

  public boolean isIntrospectCacheEnabled() {
    return introspectCacheEnabled;
  }

  public void setIntrospectCacheEnabled(boolean introspectCacheEnabled) {
    this.introspectCacheEnabled = introspectCacheEnabled;
  }

  public int getIntrospectCacheTtlSeconds() {
    return introspectCacheTtlSeconds;
  }

  public void setIntrospectCacheTtlSeconds(int introspectCacheTtlSeconds) {
    this.introspectCacheTtlSeconds = introspectCacheTtlSeconds;
  }

  public long getIntrospectCacheMaximumSize() {
    return introspectCacheMaximumSize;
  }

  public void setIntrospectCacheMaximumSize(long introspectCacheMaximumSize) {
    this.introspectCacheMaximumSize = introspectCacheMaximumSize;
  }
}
