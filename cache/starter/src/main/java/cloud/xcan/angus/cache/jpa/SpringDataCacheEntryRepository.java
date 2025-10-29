package cloud.xcan.angus.cache.jpa;

import cloud.xcan.angus.cache.entry.CacheEntry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataCacheEntryRepository extends JpaRepository<CacheEntry, Long> {

  Optional<CacheEntry> findByKey(String key);

  void deleteByKey(String key);

  @Query("SELECT c FROM CacheEntry c WHERE c.isExpired = false AND c.expireAt IS NOT NULL AND c.expireAt < CURRENT_TIMESTAMP")
  List<CacheEntry> findExpiredEntries();

  @Query("SELECT c FROM CacheEntry c WHERE c.expireAt IS NOT NULL AND c.expireAt < :expireTime")
  List<CacheEntry> findEntriesExpireBefore(@Param("expireTime") LocalDateTime expireTime);

  @Modifying
  @Query("DELETE FROM CacheEntry c WHERE c.isExpired = false AND c.expireAt IS NOT NULL AND c.expireAt < CURRENT_TIMESTAMP")
  int deleteExpiredEntries();

  @Query("SELECT COUNT(c) FROM CacheEntry c WHERE c.expireAt IS NOT NULL AND c.expireAt < CURRENT_TIMESTAMP")
  long countExpiredEntries();
}

