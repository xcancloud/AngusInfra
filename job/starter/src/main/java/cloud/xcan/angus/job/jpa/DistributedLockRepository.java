package cloud.xcan.angus.job.jpa;

import cloud.xcan.angus.job.entity.DistributedLock;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
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
  @Transactional
  @Modifying
  @Query("DELETE FROM DistributedLock l WHERE l.lockKey = :lockKey AND l.expireTime <= :now")
  int deleteExpiredLockByKey(@Param("lockKey") String lockKey, @Param("now") LocalDateTime now);

  /**
   * 删除当前节点（owner）遗留的**所有**分布式锁，包括未过期的。
   *
   * <p>应用启动时调用，清理该节点崩溃前获取的锁。由于 nodeId 使用稳定的
   * hostname-based 标识，确保同一物理节点重启后能识别并清理自己遗留的锁。
   *
   * <p>典型场景：节点 A 在持有某些 job_lock 时被强制关闭（kill -9、OOM、
   * 电源故障），这些锁的 {@code expireTime} 设得很远（如 5 分钟内不会过期）。
   * 节点 A 重启后，生成相同的 nodeId，立即调用此方法清理所有遗留的锁，
   * 避免其他节点在很长时间内无法获取这些 job。
   *
   * @param owner 当前节点的 nodeId（格式 {@code hostname|ip}）
   * @return 受影响的行数
   */
  @Transactional
  @Modifying
  @Query("DELETE FROM DistributedLock l WHERE l.owner = :owner")
  int deleteByOwner(@Param("owner") String owner);
}
