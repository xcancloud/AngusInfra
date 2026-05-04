package cloud.xcan.angus.remote;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for remote API client module.
 * <p>
 * This configuration class manages all tunable parameters for: - Feign client behavior and timeouts
 * - Request/response tracing - Idempotence key management - Security and validation settings
 * <p>
 * Usage in application.yml:
 * <pre>
 * xcan:
 *   remote:
 *     feign:
 *       connectTimeoutMillis: 5000
 *       readTimeoutMillis: 30000
 *       maxClientCacheSize: 10000
 *       clientCacheExpireMinutes: 60
 *     tracing:
 *       enabled: true
 *       traceIdHeaderName: "X-Trace-Id"
 *       spanIdHeaderName: "X-Span-Id"
 *     idempotence:
 *       enabled: true
 *       idempotenceKeyHeaderName: "Idempotence-Key"
 *       keyLength: 32
 *     validation:
 *       enableOrderByWhitelist: true
 *       maxPageSize: 1000
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "angus.remote")
@Getter
@Setter
public class RemoteProperties {

  /**
   * Feign client configuration
   */
  private FeignConfig feign = new FeignConfig();

  /**
   * Distributed tracing configuration
   */
  private TracingConfig tracing = new TracingConfig();

  /**
   * Request idempotence configuration
   */
  private IdempotenceConfig idempotence = new IdempotenceConfig();

  /**
   * Validation configuration
   */
  private ValidationConfig validation = new ValidationConfig();

  /**
   * Feign client specific configuration
   */
  @Getter
  @Setter
  public static class FeignConfig {

    /**
     * Connection timeout in milliseconds
     */
    private int connectTimeoutMillis = 5000;

    /**
     * Read timeout in milliseconds
     */
    private int readTimeoutMillis = 30000;

    /**
     * Maximum number of cached Feign clients
     */
    private int maxClientCacheSize = 10000;

    /**
     * Feign client cache expiration time in minutes
     */
    private int clientCacheExpireMinutes = 60;
  }

  /**
   * Distributed tracing configuration
   */
  @Getter
  @Setter
  public static class TracingConfig {

    /**
     * Enable distributed tracing multitenancy
     */
    private boolean enabled = true;

    /**
     * Header name for trace ID
     */
    private String traceIdHeaderName = "X-Trace-Id";

    /**
     * Header name for span ID
     */
    private String spanIdHeaderName = "X-Span-Id";

    /**
     * Header name for request ID
     */
    private String requestIdHeaderName = "X-Request-Id";
  }

  /**
   * Request idempotence configuration
   */
  @Getter
  @Setter
  public static class IdempotenceConfig {

    /**
     * Enable idempotence key multitenancy
     */
    private boolean enabled = true;

    /**
     * Header name for idempotence key
     */
    private String idempotenceKeyHeaderName = "Idempotence-Key";

    /**
     * Length of generated idempotence key
     */
    private int keyLength = 32;
  }

  /**
   * Validation configuration
   */
  @Getter
  @Setter
  public static class ValidationConfig {

    /**
     * Enable orderBy field whitelist validation
     */
    private boolean enableOrderByWhitelist = true;

    /**
     * Maximum page size allowed in pagination queries
     */
    private int maxPageSize = 1000;
  }
}
