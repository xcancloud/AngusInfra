package cloud.xcan.angus.cache.entry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations shape. The core module doesn't depend on Spring Data; the starter will
 * provide an implementation that delegates to Spring Data JPA.
 */
public interface CacheEntryRepository {

  /**
   * Find cache entry by key
   */
  Optional<CacheEntry> findByKey(String key);

  /**
   * Delete cache entry by key
   */
  void deleteByKey(String key);

  /**
   * Find all expired entries
   */
  List<CacheEntry> findExpiredEntries();

  /**
   * Find all entries that should expire before a certain time
   */
  List<CacheEntry> findEntriesExpireBefore(LocalDateTime expireTime);

  /**
   * Delete expired entries
   */
  int deleteExpiredEntries();

  /**
   * Count total entries
   */
  long count();

  /**
   * Count expired entries
   */
  long countExpiredEntries();
}
