package cloud.xcan.angus.sharding.resolver;

import cloud.xcan.angus.sharding.annotation.Sharding;

/**
 * SPI contract for resolving the numeric shard key from a jpa method invocation.
 *
 * <p>Implementations are registered as Spring beans and are automatically composed into a
 * resolution chain ordered by {@link #getOrder()}. The first resolver that returns a
 * non-{@code null} value wins; subsequent resolvers are skipped.
 *
 * <p>Built-in implementations:
 * <ul>
 *   <li>{@code DefaultShardKeyResolver} – reflection-based; handles plain field access,
 *       iterable first-element, and first {@code Long} parameter fallback.</li>
 * </ul>
 *
 * <p>Custom resolvers can be provided by registering a bean that implements this interface.
 * Example: resolving the shard key from a security context (e.g. tenant-ID from thread-local).
 */
public interface ShardKeyResolver {

  /**
   * Attempt to resolve the shard key.
   *
   * @param args       the method arguments of the intercepted jpa call
   * @param paramNames the compiled parameter names (from {@code -parameters} flag); may be
   *                   {@code null} or empty when debug info is unavailable
   * @param sharding   the {@link Sharding} annotation declared on the method
   * @param fieldName  the configured field name to look up (either {@code shardKey} or
   *                   {@code tableKey} value from the annotation)
   * @return the resolved shard key, or {@code null} when this resolver cannot handle the call
   */
  Long resolve(Object[] args, String[] paramNames, Sharding sharding, String fieldName);

  /**
   * Lower value means higher priority. Defaults to {@code 100}.
   *
   * <p>Custom resolvers should use a value lower than {@code 100} to take precedence over the
   * built-in {@code DefaultShardKeyResolver}.
   */
  default int getOrder() {
    return 100;
  }
}
