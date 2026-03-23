package cloud.xcan.angus.cache.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.cache.entry.CacheEntry;
import cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SpringCachePersistenceAdapterTest {

  private SpringDataCacheEntryRepository repository;
  private SpringCachePersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository = Mockito.mock(SpringDataCacheEntryRepository.class);
    adapter = new SpringCachePersistenceAdapter(repository);
  }

  @Test
  void findByKey_delegates() {
    CacheEntry e = sample("k");
    when(repository.findByKey("k")).thenReturn(Optional.of(e));
    assertTrue(adapter.findByKey("k").isPresent());
    verify(repository).findByKey("k");
  }

  @Test
  void save_delegates() {
    CacheEntry e = sample("s");
    when(repository.save(e)).thenReturn(e);
    assertEquals(e, adapter.save(e));
    verify(repository).save(e);
  }

  @Test
  void deleteByKey_trueWhenRowsAffected() {
    when(repository.deleteByKeyQuery("x")).thenReturn(1);
    assertTrue(adapter.deleteByKey("x"));
  }

  @Test
  void deleteByKey_falseWhenNoRows() {
    when(repository.deleteByKeyQuery("x")).thenReturn(0);
    assertFalse(adapter.deleteByKey("x"));
  }

  @Test
  void deleteAll_count_countExpired_deleteExpired_delegates() {
    when(repository.count()).thenReturn(3L);
    when(repository.countExpiredEntries()).thenReturn(1L);
    when(repository.deleteExpiredEntries()).thenReturn(2);
    assertEquals(3L, adapter.count());
    assertEquals(1L, adapter.countExpiredEntries());
    assertEquals(2, adapter.deleteExpiredEntries());
    adapter.deleteAll();
    verify(repository).deleteAll();
  }

  private static CacheEntry sample(String key) {
    return CacheEntry.builder()
        .key(key)
        .value("v")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
