package cloud.xcan.angus.security.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth2 OpenAPI 2P (Private) Authentication Configuration Properties
 *
 * This configuration class provides externalized configuration for the Feign interceptor used for
 * OpenAPI 2P (private) authentication with the OAuth2 token server.
 *
 * Configuration properties:
 * <pre>
 * xcan:
 *   auth:
 *     openapi2p:
 *       enabled: true
 *       request-path-prefix: /openapi2p
 *       token-cache-interval: 15m
 * </pre>
 *
 * Environment Variables:
 * <ul>
 *   <li>OAUTH2_OPENAPI2P_CLIENT_ID: OAuth2 client ID for OpenAPI 2P</li>
 *   <li>OAUTH2_OPENAPI2P_CLIENT_SECRET: OAuth2 client secret for OpenAPI 2P</li>
 * </ul>
 *
 * @author Framework Team
 * @version 1.0
 * @since 2025-03-21
 */
@Component
@ConfigurationProperties(prefix = "xcan.auth.openapi2p")
@Data
public class Openapi2pAuthProperties {

  /**
   * Enable or disable OpenAPI 2P authentication interceptor. Default: true (enabled when the
   * auto-configurer conditions are met)
   */
  private boolean enabled = true;

  /**
   * Request path prefix that triggers OpenAPI 2P token injection. If a request path starts with
   * this prefix, the Authorization header will be added.
   *
   * Default: "/openapi2p"
   */
  private String requestPathPrefix = DEFAULT_OPENAPI2P_PATH_PREFIX;

  /**
   * Token cache validity period. Once the token is obtained, it will be cached and reused for this
   * duration. When this period expires, a new token will be requested.
   *
   * Default: 15 minutes
   */
  private Duration tokenCacheInterval = Duration.ofMinutes(15);

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Client Authentication Configuration (Environment Variables)
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * OAuth2 OpenAPI 2P client ID environment variable name.
   *
   * IMPORTANT: Provide via environment variables to keep credentials secure.
   *
   * Example: OAUTH2_OPENAPI2P_CLIENT_ID=openapi2p-service
   */
  public static final String CLIENT_ID_ENV_PROPERTY = "OAUTH2_OPENAPI2P_CLIENT_ID";

  /**
   * OAuth2 OpenAPI 2P client secret environment variable name.
   *
   * IMPORTANT: Provide via environment variables in production. NEVER hardcode in configuration
   * files.
   *
   * Example: OAUTH2_OPENAPI2P_CLIENT_SECRET=secret-key-here
   */
  public static final String CLIENT_SECRET_ENV_PROPERTY = "OAUTH2_OPENAPI2P_CLIENT_SECRET";

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Constants
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Default OpenAPI 2P request path prefix
   */
  public static final String DEFAULT_OPENAPI2P_PATH_PREFIX = "/openapi2p";

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Utility Methods
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Check if the given path should be intercepted for token injection
   *
   * @param path the request path
   * @return true if the path starts with the configured prefix
   */
  public boolean shouldIntercept(String path) {
    if (path == null || !enabled) {
      return false;
    }
    return path.startsWith(requestPathPrefix);
  }
}
