package cloud.xcan.angus.cache.jpa;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.entity.CacheEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  public boolean deleteByKey(String key) {
    return repository.deleteByKeyQuery(key) > 0;
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

  @Override
  @Transactional(readOnly = true)
  public List<CacheEntry> findAllActive() {
    return repository.findAllActive();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CacheEntry> findAllActive(Pageable pageable) {
    return repository.findAllActive(pageable);
  }
}
