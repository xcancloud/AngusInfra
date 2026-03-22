package cloud.xcan.angus.job.repository;

import cloud.xcan.angus.job.entity.DistributedLock;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link DistributedLock}.
 */
@Repository
public interface DistributedLockRepository extends JpaRepository<DistributedLock, String> {

  /**
   * Atomically removes all locks that have expired as of {@code now}. Executed on a schedule to
   * prevent stale lock accumulation.
   *
   * @param now current server time
   * @return number of rows deleted
   */
  @Modifying
  @Query("DELETE FROM DistributedLock l WHERE l.expireTime <= :now")
  int deleteExpiredLocks(@Param("now") LocalDateTime now);

  /**
   * Removes an expired lock for the given key so that a new lock can be inserted. Operates only on
   * expired rows to preserve any concurrently acquired valid lock.
   *
   * @param lockKey target lock key
   * @param now     current server time; only rows with expireTime &le; now are affected
   * @return 1 if a row was deleted, 0 otherwise
   */
  @Modifying
  @Query("DELETE FROM DistributedLock l WHERE l.lockKey = :lockKey AND l.expireTime <= :now")
  int deleteExpiredLockByKey(@Param("lockKey") String lockKey, @Param("now") LocalDateTime now);
}
