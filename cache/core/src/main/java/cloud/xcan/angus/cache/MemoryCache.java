package cloud.xcan.angus.cache;

/**
 * @deprecated This class is dead code and will be removed in the next release.
 *     Use {@link CaffeineMemoryCache} instead, which is the active in-memory cache
 *     implementation used by {@link HybridCacheManager}.
 */
@Deprecated(forRemoval = true)
public final class MemoryCache {

  private MemoryCache() {
    // Non-instantiable — kept only to avoid breaking external compilation until removed.
    throw new UnsupportedOperationException(
        "MemoryCache is deprecated and will be removed; use CaffeineMemoryCache");
  }
}

