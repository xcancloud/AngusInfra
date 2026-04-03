package cloud.xcan.angus.job.builtin;

import cloud.xcan.angus.job.annotation.JobDefinition;
import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.jpa.JobExecutionLogRepository;
import cloud.xcan.angus.job.model.JobContext;
import cloud.xcan.angus.job.model.JobExecutionResult;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内置调度日志清理 job，每小时整点执行一次。
 *
 * <p>遍历所有启用了日志保留策略（{@code log_retention_days != -1}）的 job，
 * 根据各自配置的保留天数计算截止时间，批量删除超期的执行日志记录。
 *
 * <h3>保留策略规则</h3>
 * <ul>
 *   <li>{@code log_retention_days > 0}：保留指定天数的日志，超期记录自动删除。</li>
 *   <li>{@code log_retention_days = 0}：使用全局默认值
 *       {@link #DEFAULT_RETENTION_DAYS}（7 天）。</li>
 *   <li>{@code log_retention_days = -1}：永久保留，跳过清理。</li>
 * </ul>
 *
 * <h3>配置示例</h3>
 * <p>在 {@link JobDefinition} 中指定日志保留天数：
 * <pre>{@code
 * @JobDefinition(
 *     name = "my-heavy-job",
 *     group = "app",
 *     cron = "0 0 * * * *",
 *     logRetentionDays = 30   // 保留 30 天
 * )
 * public class MyHeavyJob implements JobExecutor { ... }
 * }</pre>
 */
@Slf4j
@Component("jobExecutionLogCleanupJob")
@JobDefinition(
    name        = "job-execution-log-cleanup-job",
    group       = "infra",
    cron        = "0 0 * * * *",   // 每小时整点触发
    maxRetryCount = 1,
    logRetentionDays = -1,          // 清理 job 自身日志永久保留，避免自我清理
    description = "调度执行日志清理任务：按各 job 配置的 logRetentionDays 删除过期执行记录，"
        + "未配置时默认保留 7 天，设为 -1 则永久保留"
)
@RequiredArgsConstructor
public class JobExecutionLogCleanupJob implements JobExecutor {

  /** 全局默认日志保留天数，当 {@code log_retention_days = 0} 时使用。 */
  static final int DEFAULT_RETENTION_DAYS = 7;

  private final JobExecutionLogRepository logRepository;

  @Override
  public JobExecutionResult execute(JobContext context) {
    // -----------------------------------------------------------------------
    // 查询所有配置了保留策略的 job（log_retention_days != -1）
    // -----------------------------------------------------------------------
    List<Object[]> jobs = logRepository.findJobIdsWithRetentionPolicy();

    int totalDeleted = 0;
    int errorCount = 0;

    for (Object[] row : jobs) {
      Long jobId = ((Number) row[0]).longValue();
      int retentionDays = ((Number) row[1]).intValue();

      // log_retention_days = 0 时回退到全局默认值
      int effectiveDays = (retentionDays == 0) ? DEFAULT_RETENTION_DAYS : retentionDays;
      LocalDateTime deadline = LocalDateTime.now().minusDays(effectiveDays);

      try {
        int deleted = logRepository.deleteByJobIdAndStartTimeBefore(jobId, deadline);
        if (deleted > 0) {
          log.info("清理 job[id={}] 过期执行日志 {} 条（保留 {} 天，截止 {}）",
              jobId, deleted, effectiveDays, deadline);
          totalDeleted += deleted;
        }
      } catch (Exception e) {
        // 单个 job 清理失败不影响其他 job，记录后继续
        log.error("清理 job[id={}] 执行日志失败", jobId, e);
        errorCount++;
      }
    }

    String summary = String.format("共扫描 %d 个 job，删除过期日志 %d 条，失败 %d 个",
        jobs.size(), totalDeleted, errorCount);
    log.info("调度日志清理完成：{}", summary);

    if (errorCount == 0) {
      return JobExecutionResult.builder()
          .success(true)
          .result(summary)
          .build();
    } else {
      return JobExecutionResult.builder()
          .success(false)
          .errorMessage(summary)
          .build();
    }
  }
}
