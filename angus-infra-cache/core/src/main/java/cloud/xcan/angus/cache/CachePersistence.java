package cloud.xcan.angus.cache;

import cloud.xcan.angus.cache.entity.CacheEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CachePersistence {

  Optional<CacheEntry> findByKey(String key);

  CacheEntry save(CacheEntry entry);

  /**
   * Deletes the entity with the given key.
   *
   * @return {@code true} if an entity was found and removed; {@code false} otherwise
   */
  boolean deleteByKey(String key);

  void deleteAll();

  long count();

  long countExpiredEntries();

  int deleteExpiredEntries();

  List<CacheEntry> findAllActive();

  Page<CacheEntry> findAllActive(Pageable pageable);
}
