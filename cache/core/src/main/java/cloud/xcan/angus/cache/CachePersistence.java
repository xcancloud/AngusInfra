package cloud.xcan.angus.cache;

import cloud.xcan.angus.cache.entry.CacheEntry;
import java.util.Optional;

public interface CachePersistence {

  Optional<CacheEntry> findByKey(String key);

  CacheEntry save(CacheEntry entry);

  void deleteByKey(String key);

  void deleteAll();

  long count();

  long countExpiredEntries();

  int deleteExpiredEntries();
}
