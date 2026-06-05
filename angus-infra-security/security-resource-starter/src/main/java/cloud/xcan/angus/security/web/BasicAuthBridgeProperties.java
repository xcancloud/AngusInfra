package cloud.xcan.angus.security.web;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for bridging non-Bearer credential carriers (HTTP Basic, custom token headers) into
 * the OAuth2 resource-server Bearer pipeline.
 *
 * <p><b>Why this exists.</b> Artifact protocol clients (docker, mvn, npm, pip, nuget, apt, yum,
 * helm) do not send {@code Authorization: Bearer <token>}. They send the token as the password of
 * an HTTP Basic header (the GitHub/GitLab/Artifactory "token as password" convention) or in a
 * protocol-specific header such as {@code X-NuGet-ApiKey}. AngusGM issues personal access tokens
 * (PAT) whose plain value <i>is</i> an OAuth2 opaque access token, so once the raw token is
 * extracted from these carriers it can be validated by the very same opaque-token introspection
 * path used for {@code Bearer} requests — no separate credential store is required.</p>
 *
 * <p><b>Disabled by default.</b> Services that do not expose artifact protocol endpoints keep the
 * stock {@code Bearer}-only behavior. AngusRepo opts in by setting {@code enabled=true}.</p>
 */
@ConfigurationProperties(prefix = "angus.security.resource.basic-bridge")
public class BasicAuthBridgeProperties {

  /**
   * When {@code true}, the resource server additionally accepts the token via HTTP Basic password
   * and the headers listed in {@link #tokenHeaders}. Default {@code false} (Bearer-only).
   */
  private boolean enabled = false;

  /**
   * When the bridge is enabled, accept an {@code Authorization: Basic} header and treat its password
   * component as the access token (username ignored). Default {@code true}.
   */
  private boolean acceptBasicPassword = true;

  /**
   * Additional request header names whose raw value is treated as the access token (e.g.
   * {@code X-NuGet-ApiKey}). Checked only when {@link #enabled} is {@code true}.
   */
  private List<String> tokenHeaders = new ArrayList<>();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isAcceptBasicPassword() {
    return acceptBasicPassword;
  }

  public void setAcceptBasicPassword(boolean acceptBasicPassword) {
    this.acceptBasicPassword = acceptBasicPassword;
  }

  public List<String> getTokenHeaders() {
    return tokenHeaders;
  }

  public void setTokenHeaders(List<String> tokenHeaders) {
    this.tokenHeaders = tokenHeaders;
  }
}
