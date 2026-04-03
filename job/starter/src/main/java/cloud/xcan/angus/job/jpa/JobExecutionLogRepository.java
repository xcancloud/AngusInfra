package cloud.xcan.angus.job.jpa;

import cloud.xcan.angus.job.entity.JobExecutionLog;
import cloud.xcan.angus.job.enums.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA jpa for {@link JobExecutionLog}.
 */
@Repository
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {

  /**
   * Returns paged execution history for a job, most recent first.
   */
  Page<JobExecutionLog> findByJobIdOrderByStartTimeDesc(Long jobId, Pageable pageable);

  /**
   * Finds jobs still in RUNNING state that started before the given threshold (used to detect
   * timed-out executions).
   *
   * @param status    RUNNING
   * @param threshold jobs that started before this time are candidates for timeout
   */
  List<JobExecutionLog> findByStatusAndStartTimeBefore(ExecutionStatus status,
      LocalDateTime threshold);

  /**
   * Returns all execution records whose {@code startTime} falls within [start, end). Used by
   * {@link cloud.xcan.angus.job.monitor.JobHealthMonitor} to compute health statistics across
   * <em>all</em> statuses for a given time window.
   *
   * @param start window start (inclusive)
   * @param end   window end (exclusive)
   */
  List<JobExecutionLog> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Deletes all execution records for the specified job. Called when a job definition is
   * permanently deleted.
   */
  @Modifying
  @Query("DELETE FROM JobExecutionLog l WHERE l.jobId = :jobId")
  void deleteByJobId(@Param("jobId") Long jobId);

  /**
   * 删除指定 job 中早于给定时间的历史执行记录。
   *
   * <p>由内置清理 job {@code job-execution-log-cleanup-job} 在每小时整点调用，
   * 根据各 job 的 {@code log_retention_days} 计算截止时间后批量删除过期日志。
   *
   * @param jobId    目标 job 的主键
   * @param deadline 截止时间：{@code startTime} 早于此值的记录将被删除
   * @return 受影响的行数
   */
  @Modifying
  @Transactional
  @Query("DELETE FROM JobExecutionLog l WHERE l.jobId = :jobId AND l.startTime < :deadline")
  int deleteByJobIdAndStartTimeBefore(@Param("jobId") Long jobId,
      @Param("deadline") LocalDateTime deadline);

  /**
   * 查询所有已配置日志保留策略的 job（即 log_retention_days != -1）的 id 与
   * log_retention_days，用于清理 job 的遍历计算。
   */
  @Query(value = "SELECT j.id, j.log_retention_days FROM angus_scheduled_job j "
      + "WHERE j.log_retention_days <> -1",
      nativeQuery = true)
  List<Object[]> findJobIdsWithRetentionPolicy();
}
