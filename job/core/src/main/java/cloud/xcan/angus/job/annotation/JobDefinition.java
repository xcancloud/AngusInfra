package cloud.xcan.angus.job.annotation;

import cloud.xcan.angus.job.enums.JobType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a {@link cloud.xcan.angus.job.executor.JobExecutor} as a self-registering scheduled
 * job.
 *
 * <p>When the application starts, {@link cloud.xcan.angus.job.registrar.JobRegistrar} scans all
 * Spring beans annotated with {@code @JobDefinition} and upserts their definitions into the
 * {@code scheduled_job} table. If a job with the same {@code name + group} already exists the
 * registration is skipped (idempotent), so restarts and re-deployments are safe.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Component("myDailyReport")
 * @JobDefinition(
 *     name        = "daily-report",
 *     group       = "report",
 *     cron        = "0 0 2 * * *",
 *     maxRetryCount = 3,
 *     description = "Generate daily report at 02:00"
 * )
 * public class DailyReportJob implements JobExecutor {
 *     @Override
 *     public JobExecutionResult execute(JobContext context) { ... }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobDefinition {

  /**
   * Human-readable job name. Combined with {@link #group()} as a unique key in
   * {@code scheduled_job}.
   */
  String name();

  /**
   * Logical group for the job (e.g. the application's short code). Defaults to {@code "default"}.
   */
  String group() default "default";

  /**
   * 6-field Spring cron expression: {@code second minute hour day month weekday}.
   *
   * <p>Uses Spring's own {@code CronExpression} parser — not Quartz's 7-field format.
   */
  String cron();

  /** Task execution model; defaults to {@link JobType#SIMPLE}. */
  JobType type() default JobType.SIMPLE;

  /**
   * Number of shards. Only meaningful for {@link JobType#SHARDING} and
   * {@link JobType#MAP_REDUCE} jobs. Defaults to {@code 1}.
   */
  int shardingCount() default 1;

  /**
   * Comma-separated shard parameters aligned with {@link #shardingCount()}. Only meaningful for
   * {@link JobType#SHARDING} and {@link JobType#MAP_REDUCE} jobs.
   */
  String shardingParameter() default "";

  /**
   * Maximum number of retry attempts on failure. {@code 0} means no retry. Defaults to {@code 3}.
   */
  int maxRetryCount() default 3;

  /**
   * Delay in seconds before the first execution after application startup. Useful for letting the
   * application warm up before a heavy job runs. Defaults to {@code 0} (run on first scheduled
   * tick).
   */
  int initialDelaySeconds() default 0;

  /** Optional human-readable description stored in {@code scheduled_job.description}. */
  String description() default "";
}
