package cloud.xcan.angus.security.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * General resource-server security knobs that are not specific to the artifact-protocol Basic
 * bridge (see {@link BasicAuthBridgeProperties}).
 *
 * <p><b>{@code allowUriQueryToken}.</b> When {@code true}, the {@code BearerTokenResolver}
 * additionally accepts the access token via the {@code ?access_token=} URL query parameter
 * (Spring's {@code DefaultBearerTokenResolver#setAllowUriQueryParameter(true)}). RFC 6750 §2.3
 * discourages this carrier because the token then leaks into access logs, reverse-proxy logs and
 * {@code Referer} headers. The default stays {@code true} for backward compatibility with services
 * that rely on query-parameter tokens (e.g. browser download / preview links); security-sensitive
 * services such as the artifact repository opt out by setting it to {@code false}.</p>
 */
@ConfigurationProperties(prefix = "angus.security.resource")
public class ResourceServerSecurityProperties {

  /**
   * Whether the access token may be supplied via the {@code ?access_token=} URL query parameter.
   * Default {@code true} (backward compatible). Set to {@code false} to follow the RFC 6750 §2.3
   * recommendation and keep tokens out of URLs / access logs.
   */
  private boolean allowUriQueryToken = true;

  public boolean isAllowUriQueryToken() {
    return allowUriQueryToken;
  }

  public void setAllowUriQueryToken(boolean allowUriQueryToken) {
    this.allowUriQueryToken = allowUriQueryToken;
  }
}
