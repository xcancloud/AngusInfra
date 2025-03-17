package cloud.xcan.sdf.core.event.source;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.enums.EventType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class UserOperation extends SimpleSource {

  private String resourceName = "";
  private String clientId;
  private String requestId = "";
  private Long userId = -1L;
  private String fullname = "";
  private Long tenantId = -1L;
  private String tenantName = "";
  private Boolean success;
  private String failureReason = "";
  @JsonFormat(pattern = DATE_FMT)
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime operationDate;

  public UserOperation() {
  }

  private UserOperation(Builder builder) {
    setType(builder.type);
    setCode(builder.code);
    setDescription(builder.description);
    setTimestamp(builder.operationDate);
    setExt(builder.ext);
    setResourceName(builder.resourceName);
    setClientId(builder.clientId);
    setRequestId(builder.requestId);
    setUserId(builder.userId);
    setFullname(builder.fullname);
    setTenantId(builder.tenantId);
    setTenantName(builder.tenantName);
    setSuccess(builder.success);
    setFailureReason(builder.failureReason);
    setOperationDate(builder.operationDate);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {

    private final EventType type = EventType.OPERATION;
    private String code;
    private String description;
    //private LocalDateTime timestamp;
    private Map<String, Object> ext;

    private String resourceName;
    private String clientId;
    private String requestId;
    private Long userId;
    private String fullname;
    private Long tenantId;
    private String tenantName;
    private Boolean success;
    private String failureReason;
    private LocalDateTime operationDate;

    private Builder() {
    }

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder ext(Map<String, Object> ext) {
      this.ext = ext;
      return this;
    }

    public Builder resourceName(String resourceName) {
      this.resourceName = resourceName;
      return this;
    }

    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    public Builder requestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder fullname(String fullname) {
      this.fullname = fullname;
      return this;
    }

    public Builder tenantId(Long tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder tenantName(String tenantName) {
      this.tenantName = tenantName;
      return this;
    }

    public Builder success(Boolean success) {
      this.success = success;
      return this;
    }

    public Builder failureReason(String failureReason) {
      this.failureReason = failureReason;
      return this;
    }

    public Builder operationDate(LocalDateTime operationDate) {
      this.operationDate = operationDate;
      return this;
    }

    public UserOperation build() {
      return new UserOperation(this);
    }
  }
}
