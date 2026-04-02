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

/**
 * Spring Data JPA repository for {@link JobExecutionLog}.
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
}
