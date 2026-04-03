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
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA jpa for {@link ScheduledJob}.
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
   * 将遗留的 RUNNING 状态任务重置为 READY（仅限当前节点遗留的任务）。
   *
   * <p>应用正常运行时不存在长期处于 RUNNING 的任务（每个任务执行完成后均会恢复为
   * READY 或置为 FAILED）。若当前节点在执行任务时被强制终止（kill -9、OOM、
   * 电源故障等），该任务的状态会永久滞留为 RUNNING，导致下次启动后调度器
   * 永远无法再次触发这些任务。
   *
   * <p>此方法**仅重置该节点之前执行但未完成的 RUNNING job**，通过检查
   * {@code JobExecutionLog} 的最新记录来判断。这避免误伤其他节点正在执行的 job。
   *
   * <p>此方法在 {@link cloud.xcan.angus.job.registrar.JobRegistrar} 启动阶段调用，
   * 确保每次应用重启后所有脏数据都能被自动清理，无需人工介入。
   *
   * @param currentNodeId 当前节点的 nodeId（格式 {@code hostname|ip}）
   * @return 受影响的行数
   */
  @Modifying
  @Transactional
  @Query(value = ""
      + "UPDATE angus_scheduled_job sj SET status = 'READY', update_time = NOW() "
      + "WHERE sj.status = 'RUNNING' "
      + "  AND sj.id IN ( "
      + "    SELECT DISTINCT jel.job_id FROM angus_job_execution_log jel "
      + "    WHERE jel.executor_node = :currentNodeId "
      + "      AND jel.status = 'RUNNING' "
      + "      AND jel.id = ( "
      + "        SELECT MAX(id) FROM angus_job_execution_log jel2 "
      + "        WHERE jel2.job_id = jel.job_id "
      + "      ) "
      + "  ) ",
      nativeQuery = true)
  int resetStaleRunningJobs(@Param("currentNodeId") String currentNodeId);
}
