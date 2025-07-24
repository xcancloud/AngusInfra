package cloud.xcan.angus.remote;

import static cloud.xcan.angus.remote.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.angus.remote.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.angus.remote.ApiConstant.OK_CODE;
import static cloud.xcan.angus.remote.message.BizException.M.BIZ_ERROR;
import static cloud.xcan.angus.remote.message.SuccessResultMessage.M.OK_MSG;

import cloud.xcan.angus.remote.message.AbstractResultMessageException;
import cloud.xcan.angus.remote.message.BizException;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Schema(description =
    """
        Represents a standard API response structure, providing status, message, data, timestamp, and extensible fields.

        Example usage:
        ```ts
        const result: ApiResult = {
          code: 'S',
          msg: 'Operation successful',
          data: { id: 1, name: 'test' },
          datetime: new Date(),
          ext: { traceId: 'abc123' }
        };
        ```"""
    , accessMode = AccessMode.READ_ONLY)
@Setter
@Getter
@Accessors(chain = true)
@ToString
@JsonInclude
public class ApiResult<T> implements Serializable {

  @Schema(description = """
      Business status code of the API response.
      - 'S': Success
      - 'E0': Protocol error (bad request)
      - 'E1': Business error
      - 'E2': System error (server error)
      - 'E3': Quota error (unauthorized or insufficient quota)
      - 'XXX': Custom business codes (require further handling)"""
  )
  private String code;

  @Schema(description = "Message providing additional context, such as success or error details.")
  private String msg;

  @Schema(description = "Actual response data or error details.")
  private T data;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Schema(description = "Server processing timestamp (date-time string).")
  private LocalDateTime datetime;

  @Schema(description = "Extensible map for extra response information.")
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

  public static ApiResult<?> success() {
    return new ApiResult<>();
  }

  public static <T> ApiResult<T> success(T data) {
    return new ApiResult<>(data);
  }

  public static ApiResult<?> success(String msg) {
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

  public ApiResult<?> orElseThrow() {
    if (isSuccess()) {
      return this;
    }
    throw BizException.of(this.code, this.getMsg());
  }

  public ApiResult<?> orElseThrow(AbstractResultMessageException exception) {
    if (isSuccess()) {
      return this;
    }
    throw exception;
  }

  public T orElseContentThrow() {
    if (isSuccess()) {
      return this.data;
    }
    throw BizException.of(this.code, this.getMsg());
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
