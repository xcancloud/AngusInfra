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
    "These parameters collectively create a structured response that provides a clear "
        + "and informative way for clients to understand the result of their API calls, including the status, "
        + "any relevant messages, the response data, processing time, and any additional context needed "
        + "for proper handling of the response.", accessMode = AccessMode.READ_ONLY)
@Setter
@Getter
@Accessors(chain = true)
@ToString
@JsonInclude
public class ApiResult<T> implements Serializable {

  @Schema(description = """
      This parameter represents the `business status code` of the API response.
      It is used to indicate the success or failure of the API request. Default code value:
      ***S***: A code of `S` indicate success;
      ***E0***: Protocol exception could indicate a bad request;
      ***E1***: Business exception could indicate a bad business handle;
      ***E2***: System exception could signify a server error;
      ***E3***: Quota exception could indicate unauthorized quota or insufficient quota;
      ***XXX***: Custom business status codes for content usually require secondary confirmation or processing by the caller."""
  )
  private String code;

  @Schema(description = "This parameter contains a message that provides additional context "
      + "or information about the API response. This could be a success message, an error description, "
      + "or any other relevant information that helps the client understand the result of the request.")
  private String msg;

  @Schema(description = "This parameter holds the actual data returned by the API,"
      + " which is typically the result or exception trace of the requested operation. ")
  private T data;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Schema(description =
      "This parameter represents the server's processing time for the request, formatted as a date-time string. "
          + "This information can be useful for clients to log when the request was processed or for troubleshooting purposes, "
          + "as it indicates how long the server took to respond.")
  private LocalDateTime datetime;

  @Schema(description = "This parameter is an extensible map that can hold additional information "
      + "related to the response that doesn't fit into the predefined fields. "
      + "It allows for flexibility in the API response by enabling the inclusion of extra data "
      + "that may be useful for the client.")
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
