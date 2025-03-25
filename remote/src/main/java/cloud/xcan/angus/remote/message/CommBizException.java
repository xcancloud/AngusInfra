package cloud.xcan.angus.remote.message;

import static cloud.xcan.angus.remote.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.ERROR;
import static cloud.xcan.angus.remote.message.CommBizException.M.BIZ_ERROR;
import static cloud.xcan.angus.remote.message.CommBizException.M.BIZ_ERROR_KEY;

import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.api.enums.EventType;
import lombok.Getter;
import lombok.ToString;

/**
 * General business exception class, which will be handled by global exception and return http
 * status 200.
 */
@Getter
@ToString
public class CommBizException extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public CommBizException() {
    this(BIZ_ERROR, BIZ_ERROR_KEY, null, ERROR);
  }

  public CommBizException(String message, String eKey, Object[] args, ExceptionLevel level) {
    this(message, eKey, args, level, null);
  }

  public CommBizException(String message, String eKey, Object[] args, ExceptionLevel level,
      Throwable cause) {
    super(message, EventType.BUSINESS, level, eKey, cause);
    this.code = BUSINESS_ERROR_CODE;
    this.msg = message;
    this.args = args;
  }

  public static CommBizException of(String message) {
    return new CommBizException(message, BIZ_ERROR_KEY, null, ERROR);
  }

  public static CommBizException of(String message, Throwable cause) {
    return new CommBizException(message, BIZ_ERROR_KEY, null, ERROR, cause);
  }

  public static CommBizException of(String message, String eKey) {
    return new CommBizException(message, eKey, null, ERROR);
  }

  public static CommBizException of(String message, String eKey, Throwable cause) {
    return new CommBizException(message, eKey, null, ERROR, cause);
  }

  public static CommBizException of(String message, Object[] agrs) {
    return new CommBizException(message, BIZ_ERROR_KEY, agrs, ERROR);
  }

  public static CommBizException of(String message, Object[] agrs, Throwable cause) {
    return new CommBizException(message, BIZ_ERROR_KEY, agrs, ERROR, cause);
  }

  public static CommBizException of(String message, String eKey, Object[] agrs) {
    return new CommBizException(message, eKey, agrs, ERROR);
  }

  public static CommBizException of(String message, String eKey, Object[] agrs, Throwable cause) {
    return new CommBizException(message, eKey, agrs, ERROR, cause);
  }

  public static CommBizException of(ExceptionLevel level) {
    return new CommBizException(BIZ_ERROR, BIZ_ERROR_KEY, null, level);
  }

  public static CommBizException of(ExceptionLevel level, Throwable cause) {
    return new CommBizException(BIZ_ERROR, BIZ_ERROR_KEY, null, level, cause);
  }

  public static CommBizException of(String message, ExceptionLevel level) {
    return new CommBizException(message, BIZ_ERROR_KEY, null, level);
  }

  public static CommBizException of(String message, ExceptionLevel level, Throwable cause) {
    return new CommBizException(message, BIZ_ERROR_KEY, null, level, cause);
  }

  public static CommBizException of(String message, String eKey, ExceptionLevel level) {
    return new CommBizException(message, eKey, null, level);
  }

  public static CommBizException of(String message, String eKey, ExceptionLevel level,
      Throwable cause) {
    return new CommBizException(message, eKey, null, level, cause);
  }

  public static CommBizException of(String message, Object[] agrs, ExceptionLevel level) {
    return new CommBizException(message, BIZ_ERROR_KEY, agrs, level);
  }

  public static CommBizException of(String message, Object[] agrs, ExceptionLevel level,
      Throwable cause) {
    return new CommBizException(message, BIZ_ERROR_KEY, agrs, level, cause);
  }

  public static CommBizException of(String message, String eKey, Object[] agrs,
      ExceptionLevel level) {
    return new CommBizException(message, eKey, agrs, level);
  }

  public static CommBizException of(String message, String eKey, Object[] agrs,
      ExceptionLevel level, Throwable cause) {
    return new CommBizException(message, eKey, agrs, level, cause);
  }

  public interface M {

    String BIZ_ERROR = "xcm.biz.error";
    String BIZ_ERROR_KEY = "business_error";

  }

}
