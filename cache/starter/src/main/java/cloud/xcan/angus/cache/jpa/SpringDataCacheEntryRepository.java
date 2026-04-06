package cloud.xcan.angus.cache.jpa;

import cloud.xcan.angus.cache.entity.CacheEntry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataCacheEntryRepository extends JpaRepository<CacheEntry, Long> {

  Optional<CacheEntry> findByKey(String key);

  @Modifying
  @Query("DELETE FROM CacheEntry c WHERE c.key = :key")
  int deleteByKeyQuery(@Param("key") String key);

  @Query("SELECT c FROM CacheEntry c WHERE c.expireAt IS NOT NULL AND c.expireAt < CURRENT_TIMESTAMP")
  List<CacheEntry> findExpiredEntries();

  @Query("SELECT c FROM CacheEntry c WHERE c.expireAt IS NOT NULL AND c.expireAt < :expireTime")
  List<CacheEntry> findEntriesExpireBefore(@Param("expireTime") LocalDateTime expireTime);

  @Modifying
  @Query("DELETE FROM CacheEntry c WHERE c.expireAt IS NOT NULL AND c.expireAt < CURRENT_TIMESTAMP")
  int deleteExpiredEntries();

  @Query("SELECT COUNT(c) FROM CacheEntry c WHERE c.expireAt IS NOT NULL AND c.expireAt < CURRENT_TIMESTAMP")
  long countExpiredEntries();

  @Query("SELECT c FROM CacheEntry c WHERE c.expireAt IS NULL OR c.expireAt >= CURRENT_TIMESTAMP ORDER BY c.updatedAt DESC")
  List<CacheEntry> findAllActive();

  @Query("SELECT c FROM CacheEntry c WHERE c.expireAt IS NULL OR c.expireAt >= CURRENT_TIMESTAMP ORDER BY c.updatedAt DESC")
  Page<CacheEntry> findAllActive(Pageable pageable);
}

