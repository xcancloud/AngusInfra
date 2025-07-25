package cloud.xcan.angus.lettucex.config;

import cloud.xcan.angus.api.enums.RedisDeployment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Extended Redis configuration properties that build upon Spring Boot's standard
 * Redis configuration. This class adds custom deployment options and feature toggles
 * specific to the application's Redis integration requirements.
 * </p>
 * 
 * <p>
 * Key features:
 * - Extends Spring Boot's standard Redis properties
 * - Support for different Redis deployment modes (single, cluster, sentinel)
 * - Global enable/disable toggle for Redis functionality
 * - Integration with custom Redis deployment strategies
 * </p>
 * 
 * <p>
 * Configuration example:
 * <pre>
 * xcan:
 *   redis:
 *     enabled: true
 *     deployment: SINGLE
 *     host: localhost
 *     port: 6379
 *     database: 0
 *     timeout: 2000ms
 * </pre>
 * </p>
 * 
 * <p>
 * This class inherits all standard Redis configuration options from Spring Boot
 * including connection settings, pool configuration, SSL settings, and more.
 * </p>
 * 
 * @see org.springframework.boot.autoconfigure.data.redis.RedisProperties
 * @see RedisDeployment
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "xcan.redis", ignoreUnknownFields = false)
public class RedisProperties extends
    org.springframework.boot.autoconfigure.data.redis.RedisProperties {

  /**
   * <p>
   * Global switch to enable or disable Redis functionality.
   * When disabled, Redis-related beans and configurations will not be initialized.
   * </p>
   * 
   * <p>
   * This is useful for:
   * - Development environments without Redis
   * - Testing scenarios with alternative implementations
   * - Gradual rollout of Redis features
   * - Fallback scenarios when Redis is unavailable
   * </p>
   */
  private Boolean enabled = false;

  /**
   * <p>
   * Specifies the Redis deployment architecture being used.
   * This setting influences connection management, failover behavior,
   * and load balancing strategies.
   * </p>
   * 
   * <p>
   * Supported deployment modes:
   * - SINGLE: Single Redis instance (default)
   * - CLUSTER: Redis cluster for horizontal scaling
   * - SENTINEL: Redis Sentinel for high availability
   * </p>
   * 
   * <p>
   * The deployment mode affects:
   * - Connection pool configuration
   * - Failover and retry logic
   * - Data partitioning strategies
   * - Monitoring and health checks
   * </p>
   */
  private RedisDeployment deployment = RedisDeployment.SINGLE;
}
