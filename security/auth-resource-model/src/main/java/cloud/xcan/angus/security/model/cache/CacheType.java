package cloud.xcan.angus.security.model.cache;

/**
 * Token cache storage type.
 *
 * <p>Controls whether tokens are cached locally (in-memory, single instance)
 * or in a distributed cache (shared across multiple instances).</p>
 *
 * @author Framework Team
 * @version 1.0
 * @since 2025-03-22
 */
public enum CacheType {

  /**
   * In-memory token cache using volatile fields.
   * Suitable for single-instance deployments.
   */
  LOCAL,

  /**
   * Distributed token cache using {@code IDistributedCache}.
   * Suitable for multi-instance deployments.
   * Requires {@code xcan-infra.cache} module on the classpath.
   */
  DISTRIBUTED
}
