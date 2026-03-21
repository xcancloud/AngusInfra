package cloud.xcan.angus.cache.autoconfigure;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.entry.CacheEntry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory-only fallback implementation of {@link CachePersistence}.
 *
 * <p>Registered automatically when no JPA {@code SpringDataCacheEntryRepository} bean is present,
 * allowing the cache module to operate in pure memory mode without a database. All data is lost on
 * application restart. For production use with durability requirements, configure a real JPA
 * persistence adapter instead.
 */
@Slf4j
public class NoOpCachePersistence implements CachePersistence {

  private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

  public NoOpCachePersistence() {
    log.info("Cache running in memory-only mode (no JPA persistence configured).");
  }

  @Override
  public Optional<CacheEntry> findByKey(String key) {
    return Optional.ofNullable(store.get(key));
  }

  @Override
  public CacheEntry save(CacheEntry entry) {
    store.put(entry.getKey(), entry);
    return entry;
  }

  @Override
  public boolean deleteByKey(String key) {
    return store.remove(key) != null;
  }

  @Override
  public void deleteAll() {
    store.clear();
  }

  @Override
  public long count() {
    return store.size();
  }

  @Override
  public long countExpiredEntries() {
    return store.values().stream().filter(CacheEntry::hasExpired).count();
  }

  @Override
  public int deleteExpiredEntries() {
    int before = store.size();
    store.entrySet().removeIf(e -> e.getValue().hasExpired());
    return before - store.size();
  }
}
