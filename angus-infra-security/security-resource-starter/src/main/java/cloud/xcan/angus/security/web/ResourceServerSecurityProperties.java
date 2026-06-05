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
 * {@code Referer} headers. The default is therefore {@code false} (the RFC-recommended secure
 * default); services that still rely on query-parameter tokens (e.g. legacy browser download /
 * preview links) opt back in by setting it to {@code true}.</p>
 */
@ConfigurationProperties(prefix = "angus.security.resource")
public class ResourceServerSecurityProperties {

  /**
   * Whether the access token may be supplied via the {@code ?access_token=} URL query parameter.
   * Default {@code false} per the RFC 6750 §2.3 recommendation (keeps tokens out of URLs / access
   * logs). Set to {@code true} to restore the legacy query-parameter carrier.
   */
  private boolean allowUriQueryToken = false;

  public boolean isAllowUriQueryToken() {
    return allowUriQueryToken;
  }

  public void setAllowUriQueryToken(boolean allowUriQueryToken) {
    this.allowUriQueryToken = allowUriQueryToken;
  }
}
