
package cloud.xcan.sdf.core.biz.exception;

import static cloud.xcan.sdf.api.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.sdf.api.ExceptionLevel.ERROR;
import static cloud.xcan.sdf.api.message.CommBizException.M.BIZ_ERROR;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.message.CommBizException;
import lombok.Getter;
import lombok.ToString;

/**
 * Mark business operations or transactions that do not need to be rolled back.
 */
@Getter
@ToString
public class NoRollbackException extends CommBizException {

  private final String code;
  private final String msg;
  private final Object[] agrs;

  public NoRollbackException() {
    this(BUSINESS_ERROR_CODE, BIZ_ERROR, null, null, ERROR);
  }

  private NoRollbackException(String code, String message, String eKey, Object[] agrs,
      ExceptionLevel level) {
    super(message, eKey, agrs, level);
    this.code = code;
    this.msg = message;
    this.agrs = agrs;
  }

  public static NoRollbackException of(String message) {
    return new NoRollbackException(BUSINESS_ERROR_CODE, message, null, null, ERROR);
  }

  public static NoRollbackException of(String message, Object[] agrs) {
    return new NoRollbackException(BUSINESS_ERROR_CODE, message, null, agrs, ERROR);
  }

  public static NoRollbackException of(String code, String message) {
    return new NoRollbackException(code, message, null, null, ERROR);
  }

  public static NoRollbackException of(String code, String message, Object[] agrs) {
    return new NoRollbackException(code, message, null, agrs, ERROR);
  }

  public static NoRollbackException of(String message, ExceptionLevel level) {
    return new NoRollbackException(BUSINESS_ERROR_CODE, message, null, null, level);
  }

  public static NoRollbackException of(String message, Object[] agrs, ExceptionLevel level) {
    return new NoRollbackException(BUSINESS_ERROR_CODE, message, null, agrs, level);
  }

  public static NoRollbackException of(String code, String message, ExceptionLevel level) {
    return new NoRollbackException(code, message, null, null, level);
  }

  public static NoRollbackException of(String code, String message, Object[] agrs,
      ExceptionLevel level) {
    return new NoRollbackException(code, message, null, agrs, level);
  }

}
