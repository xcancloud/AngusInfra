package cloud.xcan.sdf.api;

import static cloud.xcan.sdf.api.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.sdf.api.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.sdf.api.ApiConstant.OK_CODE;
import static cloud.xcan.sdf.api.message.CommBizException.M.BIZ_ERROR;
import static cloud.xcan.sdf.api.message.SuccessResultMessage.M.OK_MSG;

import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import cloud.xcan.sdf.api.message.CommBizException;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@ToString
@JsonInclude
public class ApiResult<T> implements Serializable {

  @Schema(description = "TenantStatus code")
  private String code;

  @Schema(description = "TenantStatus message")
  private String msg;

  @Schema(description = "Data or exception trace")
  private T data;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Schema(description = "Service return time")
  private LocalDateTime datetime;

  @Schema(description = "Additional data")
  private Map<String, Object> ext = new HashMap<>();

  public ApiResult() {
    this(OK_CODE, OK_MSG, null, null);
  }

  public ApiResult(T data) {
    this(OK_CODE, OK_MSG, data, null);
  }

  public ApiResult(String code, String msg) {
    this(code, msg, null, null);
  }

  public ApiResult(String code, String msg, T data) {
    this(code, msg, data, null);
  }

  public ApiResult(String code, String msg, T data, Map<String, Object> ext) {
    this.code = code;
    this.msg = msg;
    this.data = data;
    this.datetime = LocalDateTime.now();
    if (Objects.nonNull(ext)) {
      this.ext = ext;
    }
  }

  public static ApiResult success() {
    return new ApiResult<>();
  }

  public static <T> ApiResult<T> success(T data) {
    return new ApiResult<>(data);
  }

  public static ApiResult success(String msg) {
    return new ApiResult<>(OK_CODE, msg, null);
  }

  public static <T> ApiResult<T> success(String msg, T data) {
    return new ApiResult<>(OK_CODE, msg, data);
  }

  public static <T> ApiResult<T> error() {
    return new ApiResult<>(BUSINESS_ERROR_CODE, BIZ_ERROR);
  }

  public static <T> ApiResult<T> error(String msg) {
    return new ApiResult<>(BUSINESS_ERROR_CODE, msg);
  }

  public static <T> ApiResult<T> error(String code, String msg) {
    return new ApiResult<>(code, msg);
  }

  public static <T> ApiResult<T> error(String code, String msg, T data) {
    return new ApiResult<>(code, msg, data);
  }

  public static <T> ApiResult<T> error(String code, String msg, T data,
      Map<String, Object> ext) {
    return new ApiResult<>(code, msg, data, ext);
  }

  @JsonIgnore
  public boolean isSuccess() {
    return OK_CODE.equals(this.code);
  }

  public ApiResult orElseThrow() {
    if (isSuccess()) {
      return this;
    }
    throw CommBizException.of(this.code, this.getMsg());
  }

  public ApiResult orElseThrow(AbstractResultMessageException exception) {
    if (isSuccess()) {
      return this;
    }
    throw exception;
  }

  public T orElseContentThrow() {
    if (isSuccess()) {
      return this.data;
    }
    throw CommBizException.of(this.code, this.getMsg());
  }

  public T orElseContentThrow(AbstractResultMessageException exception) {
    if (isSuccess()) {
      return this.data;
    }
    throw exception;
  }

  @JsonIgnore
  public String getEKey() {
    Object eKey = this.getExt().get(EXT_EKEY_NAME);
    return Objects.nonNull(eKey) ? String.valueOf(eKey) : null;
  }

}
