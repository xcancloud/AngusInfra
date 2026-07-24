package cloud.xcan.angus.security.introspection;

/**
 * Invalidates in-process opaque-token introspection result cache entries.
 * <p>Used after logout / token revoke so the local resource-server filter does not keep accepting
 * a deleted token until TTL expiry.
 */
public interface IntrospectionCacheInvalidator {

  /**
   * Drop any cached principal for the given access token (no-op if caching is disabled / miss).
   */
  void invalidateToken(String token);
}
