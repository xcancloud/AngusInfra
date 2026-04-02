package cloud.xcan.angus.job.jpa;

import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.JobStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link ScheduledJob}.
 */
@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {

  /**
   * Finds all jobs due for execution: status is READY and scheduled time is in the past.
   *
   * @param status READY
   * @param time   current time
   * @return list of overdue jobs
   */
  List<ScheduledJob> findByStatusAndNextExecuteTimeBefore(JobStatus status, LocalDateTime time);

  /**
   * Bounded variant — returns at most {@code pageable.getPageSize()} due jobs. Used by the
   * scheduler to prevent loading an unbounded result set when many jobs become due simultaneously.
   */
  List<ScheduledJob> findByStatusAndNextExecuteTimeBefore(JobStatus status, LocalDateTime time,
      Pageable pageable);

  Optional<ScheduledJob> findByJobNameAndJobGroup(String jobName, String jobGroup);

  /**
   * 将所有遗留的 RUNNING 状态任务重置为 READY，并将 update_time 更新为当前时间。
   *
   * <p>应用正常运行时不存在长期处于 RUNNING 的任务（每个任务执行完成后均会恢复为
   * READY 或置为 FAILED）。若应用崩溃或被强制终止，正在执行的任务状态将滞留为 RUNNING，
   * 导致下次启动后调度器永远无法再次触发这些任务。
   *
   * <p>此方法在 {@link cloud.xcan.angus.job.registrar.JobRegistrar} 启动阶段调用，
   * 确保每次应用重启后所有脏数据都能被自动清理，无需人工介入。
   *
   * @return 受影响的行数
   */
  @Modifying
  @Transactional
  @Query(value = "UPDATE angus_scheduled_job SET status = 'READY', update_time = NOW() WHERE status = 'RUNNING'",
      nativeQuery = true)
  int resetStaleRunningJobs();
}
