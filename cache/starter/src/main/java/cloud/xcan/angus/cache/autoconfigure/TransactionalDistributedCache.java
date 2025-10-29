package cloud.xcan.angus.cache.autoconfigure;

import cloud.xcan.angus.cache.CacheStats;
import cloud.xcan.angus.cache.IDistributedCache;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalDistributedCache implements IDistributedCache {

  private final IDistributedCache delegate;

  public TransactionalDistributedCache(IDistributedCache delegate) {
    this.delegate = delegate;
  }

  @Override
  @Transactional
  public void set(String key, String value, Long ttlSeconds) {
    delegate.set(key, value, ttlSeconds);
  }

  @Override
  @Transactional
  public void set(String key, String value) {
    delegate.set(key, value);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> get(String key) {
    return delegate.get(key);
  }

  @Override
  @Transactional
  public boolean delete(String key) {
    return delegate.delete(key);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean exists(String key) {
    return delegate.exists(key);
  }

  @Override
  @Transactional(readOnly = true)
  public long getTTL(String key) {
    return delegate.getTTL(key);
  }

  @Override
  @Transactional
  public boolean expire(String key, long ttlSeconds) {
    return delegate.expire(key, ttlSeconds);
  }

  @Override
  @Transactional
  public void clear() {
    delegate.clear();
  }

  @Override
  @Transactional(readOnly = true)
  public CacheStats getStats() {
    return delegate.getStats();
  }

  @Override
  @Transactional
  public int cleanupExpiredEntries() {
    return delegate.cleanupExpiredEntries();
  }
}

