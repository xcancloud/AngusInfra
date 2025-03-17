package cloud.xcan.sdf.api;

import static cloud.xcan.sdf.api.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.sdf.api.ApiConstant.OK_CODE;
import static cloud.xcan.sdf.api.message.CommBizException.M.BIZ_ERROR;
import static cloud.xcan.sdf.api.message.SuccessResultMessage.M.OK_MSG;

import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import cloud.xcan.sdf.api.message.CommBizException;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class ApiLocaleResult<T> extends ApiResult<T> {

  public ApiLocaleResult() {
    this(OK_CODE, MessageHolder.message(OK_MSG), null, null);
  }

  public ApiLocaleResult(T data) {
    this(OK_CODE, MessageHolder.message(OK_MSG), data, null);
  }

  public ApiLocaleResult(String code, String msg) {
    this(code, MessageHolder.message(msg), null, null);
  }

  public ApiLocaleResult(String code, String msg, T data) {
    this(code, msg, data, null);
  }

  public ApiLocaleResult(String code, String msg, T data, Map<String, Object> ext) {
    super(code, msg, data, ext);
  }

  public static ApiLocaleResult success() {
    return new ApiLocaleResult<>();
  }

  public static <T> ApiLocaleResult<T> success(T data) {
    return new ApiLocaleResult<>(data);
  }

  public static ApiLocaleResult<?> success(String msg) {
    return new ApiLocaleResult<>(OK_CODE, msg, null);
  }

  public static <T> ApiLocaleResult<T> success(String msg, T data) {
    return new ApiLocaleResult<>(OK_CODE, msg, data);
  }

  public static <T> ApiLocaleResult<T> successData(T data) {
    return new ApiLocaleResult<>(data);
  }

  public static <T> ApiLocaleResult<T> error() {
    return new ApiLocaleResult<>(BUSINESS_ERROR_CODE, BIZ_ERROR);
  }

  public static <T> ApiLocaleResult<T> errorData(T data) {
    return new ApiLocaleResult<>(BUSINESS_ERROR_CODE, BIZ_ERROR, data);
  }

  public static <T> ApiLocaleResult<T> error(String msg) {
    return new ApiLocaleResult<>(BUSINESS_ERROR_CODE, msg);
  }

  public static <T> ApiLocaleResult<T> error(String code, String msg) {
    return new ApiLocaleResult<>(code, msg);
  }

  public static <T> ApiLocaleResult<T> error(String code, String msg, T data) {
    return new ApiLocaleResult<>(code, msg, data);
  }

  public static <T> ApiResult<T> error(String code, String msg, T data,
      Map<String, Object> ext) {
    return new ApiLocaleResult<>(code, msg, data, ext);
  }

  @Override
  @JsonIgnore
  public boolean isSuccess() {
    return OK_CODE.equals(super.getCode());
  }

  @Override
  public ApiLocaleResult<T> orElseThrow() {
    if (isSuccess()) {
      return this;
    }
    throw CommBizException.of(this.getCode(), this.getMsg());
  }

  @Override
  public ApiLocaleResult<T> orElseThrow(AbstractResultMessageException exception) {
    if (isSuccess()) {
      return this;
    }
    throw exception;
  }

  @Override
  public T orElseContentThrow() {
    if (isSuccess()) {
      return this.getData();
    }
    throw CommBizException.of(this.getCode(), this.getMsg());
  }

  @Override
  public T orElseContentThrow(AbstractResultMessageException exception) {
    if (isSuccess()) {
      return this.getData();
    }
    throw exception;
  }
}
