package cloud.xcan.angus.security.model.cache;

import java.util.Optional;

/**
 * Abstraction for token storage, supporting both local (in-memory) and distributed cache backends.
 *
 * <p>Implementations must be thread-safe. The choice between local and distributed storage is
 * controlled by the {@code cache-type} configuration property of each auth module.</p>
 *
 * <p>Configuration example:</p>
 * <pre>
 * xcan:
 *   auth:
 *     innerapi:
 *       cache-type: local    # or "distributed" for multi-instance deployments
 * </pre>
 *
 * @author Framework Team
 * @version 1.0
 * @since 2025-03-22
 */
public interface TokenStore {

  /**
   * Store a token with the given key and TTL.
   *
   * @param key        unique cache key (e.g. "auth:innerapi:token")
   * @param token      the OAuth2 Bearer token string
   * @param ttlSeconds time-to-live in seconds
   */
  void store(String key, String token, long ttlSeconds);

  /**
   * Retrieve a cached token by key.
   *
   * @param key cache key
   * @return the token if present and not expired, otherwise empty
   */
  Optional<String> retrieve(String key);

  /**
   * Remove a cached token by key.
   *
   * @param key cache key
   */
  void remove(String key);

  /**
   * Check whether a non-expired token exists for the given key.
   *
   * @param key cache key
   * @return {@code true} if a valid token exists
   */
  boolean exists(String key);
}
