package cloud.xcan.angus.job.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for updating mutable fields of an existing scheduled job.
 *
 * <p>Status and execution-tracking fields are managed by the scheduler and
 * cannot be updated via this DTO.
 */
@Data
public class UpdateJobRequest {

  @NotBlank(message = "jobName must not be blank")
  @Size(max = 255)
  private String jobName;

  @NotBlank(message = "cronExpression must not be blank")
  @Size(max = 255)
  private String cronExpression;

  @Size(max = 4000)
  private String description;
}
