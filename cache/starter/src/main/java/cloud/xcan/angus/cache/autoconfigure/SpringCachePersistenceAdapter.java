package cloud.xcan.angus.cache.autoconfigure;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.entry.CacheEntry;
import cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public class SpringCachePersistenceAdapter implements CachePersistence {

  private final SpringDataCacheEntryRepository repository;

  public SpringCachePersistenceAdapter(SpringDataCacheEntryRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CacheEntry> findByKey(String key) {
    return repository.findByKey(key);
  }

  @Override
  @Transactional
  public CacheEntry save(CacheEntry entry) {
    return repository.save(entry);
  }

  @Override
  @Transactional
  public void deleteByKey(String key) {
    repository.deleteByKey(key);
  }

  @Override
  @Transactional
  public void deleteAll() {
    repository.deleteAll();
  }

  @Override
  @Transactional(readOnly = true)
  public long count() {
    return repository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public long countExpiredEntries() {
    return repository.countExpiredEntries();
  }

  @Override
  @Transactional
  public int deleteExpiredEntries() {
    return repository.deleteExpiredEntries();
  }
}
