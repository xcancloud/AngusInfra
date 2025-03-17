package cloud.xcan.sdf.core.biz.exception;

import static cloud.xcan.sdf.api.ApiConstant.ECode.QUOTA_ERROR_CODE;
import static cloud.xcan.sdf.api.ExceptionLevel.ERROR;
import static cloud.xcan.sdf.core.biz.exception.QuotaException.M.QUOTA_ERROR;
import static cloud.xcan.sdf.core.biz.exception.QuotaException.M.QUOTA_ERROR_KEY;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import lombok.Getter;
import lombok.ToString;

/**
 * Insufficient resource quota exception.
 */
@Getter
@ToString
public class QuotaException extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public QuotaException() {
    this(QUOTA_ERROR_CODE, QUOTA_ERROR, null, null);
  }

  private QuotaException(String code, String message, Object[] args, ExceptionLevel level) {
    super(message, EventType.QUOTA, level, QUOTA_ERROR_KEY);
    this.code = code;
    this.msg = message;
    this.args = args;
  }

  public static QuotaException of(String message) {
    return new QuotaException(QUOTA_ERROR_CODE, message, null, ERROR);
  }

  public static QuotaException of(String message, Object[] agrs) {
    return new QuotaException(QUOTA_ERROR_CODE, message, agrs, ERROR);
  }

  public static QuotaException of(String code, String message) {
    return new QuotaException(code, message, null, ERROR);
  }

  public static QuotaException of(String code, String message, Object[] agrs) {
    return new QuotaException(code, message, agrs, ERROR);
  }

  public static QuotaException of(String message, ExceptionLevel level) {
    return new QuotaException(QUOTA_ERROR_CODE, message, null, level);
  }

  public static QuotaException of(String message, Object[] agrs, ExceptionLevel level) {
    return new QuotaException(QUOTA_ERROR_CODE, message, agrs, level);
  }

  public static QuotaException of(String code, String message, ExceptionLevel level) {
    return new QuotaException(code, message, null, level);
  }

  public static QuotaException of(String code, String message, Object[] agrs,
      ExceptionLevel level) {
    return new QuotaException(code, message, agrs, level);
  }

  public interface M {

    String QUOTA_ERROR = "xcm.quota.error";
    String QUOTA_ERROR_KEY = "quota_error";
    String QUOTA_OVER_LIMIT_T = "xcm.quota.over.limit.t";
    String QUOTA_OVER_LIMIT_T2 = "xcm.quota.over.limit.t2";

  }
}
