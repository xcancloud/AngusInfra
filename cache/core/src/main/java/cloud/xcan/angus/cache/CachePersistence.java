package cloud.xcan.angus.cache;

import cloud.xcan.angus.cache.entry.CacheEntry;
import java.util.Optional;

public interface CachePersistence {

  Optional<CacheEntry> findByKey(String key);

  CacheEntry save(CacheEntry entry);

  /**
   * Deletes the entry with the given key.
   *
   * @return {@code true} if an entry was found and removed; {@code false} otherwise
   */
  boolean deleteByKey(String key);

  void deleteAll();

  long count();

  long countExpiredEntries();

  int deleteExpiredEntries();
}
