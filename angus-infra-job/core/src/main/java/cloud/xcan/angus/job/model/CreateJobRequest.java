package cloud.xcan.angus.job.model;

import cloud.xcan.angus.job.enums.JobType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for creating a new scheduled job.
 *
 * <p>Using a dedicated DTO instead of accepting the JPA entity directly prevents
 * mass-assignment vulnerabilities and decouples the API contract from the persistence model.
 */
@Data
public class CreateJobRequest {

  @NotBlank(message = "jobName must not be blank")
  @Size(max = 255, message = "jobName must not exceed 255 characters")
  private String jobName;

  @NotBlank(message = "jobGroup must not be blank")
  @Size(max = 255, message = "jobGroup must not exceed 255 characters")
  private String jobGroup;

  /**
   * Standard cron expression (6 fields: second minute hour day month weekday). Validated at runtime
   * by {@code CronExpression.parse}.
   */
  @NotBlank(message = "cronExpression must not be blank")
  @Size(max = 255)
  private String cronExpression;

  /**
   * Spring bean name of the executor; must be registered in
   * {@link cloud.xcan.angus.job.executor.JobExecutorRegistry}. Only alphanumeric characters,
   * hyphens, and underscores are permitted to prevent bean-name injection.
   */
  @NotBlank(message = "beanName must not be blank")
  @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "beanName may only contain letters, digits, hyphens and underscores")
  @Size(max = 255)
  private String beanName;

  @NotNull(message = "jobType must not be null")
  private JobType jobType;

  @Min(value = 1, message = "shardingCount must be at least 1")
  private Integer shardingCount;

  @Size(max = 4000)
  private String shardingParameter;

  @Min(value = 0)
  private Integer maxRetryCount;

  @Size(max = 4000)
  private String description;

  /**
   * 执行日志保留天数。{@code 0} 表示使用全局默认值（7 天），{@code -1} 表示永久保留。
   */
  @Min(value = -1, message = "logRetentionDays must be >= -1 (-1 means keep forever)")
  private Integer logRetentionDays;
}
