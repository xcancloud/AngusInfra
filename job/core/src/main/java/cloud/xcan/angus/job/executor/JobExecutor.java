package cloud.xcan.angus.job.executor;

import cloud.xcan.angus.job.model.JobContext;
import cloud.xcan.angus.job.model.JobExecutionResult;

/**
 * SPI contract for a scheduled job executor.
 *
 * <p>Implementations must be Spring beans so that they can be discovered and
 * registered with {@link JobExecutorRegistry}.  The bean name used in the
 * {@link cloud.xcan.angus.job.entity.ScheduledJob#getBeanName()} field must match the Spring
 * component name exactly.
 *
 * <p>Example:
 * <pre>{@code
 * @Component("myDailyReport")
 * public class DailyReportJob implements JobExecutor {
 *     @Override
 *     public JobExecutionResult execute(JobContext context) { ... }
 * }
 * }</pre>
 */
public interface JobExecutor {

  /**
   * Executes the job with the supplied context.
   *
   * @param context runtime information about the current execution attempt
   * @return a non-null result describing success or failure
   */
  JobExecutionResult execute(JobContext context);
}
