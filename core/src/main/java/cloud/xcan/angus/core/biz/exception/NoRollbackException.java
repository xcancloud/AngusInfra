package cloud.xcan.angus.core.biz.exception;

import static cloud.xcan.angus.remote.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.ERROR;
import static cloud.xcan.angus.remote.message.BizException.M.BIZ_ERROR;

import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.BizException;
import lombok.Getter;
import lombok.ToString;

/**
 * Marker subclass of {@link cloud.xcan.angus.remote.message.BizException} for flows where the
 * transaction should not roll back. The optional {@link #code} on this class is stored locally; the
 * parent still uses the standard remote business error key for messaging.
 */
@Getter
@ToString
public class NoRollbackException extends BizException {

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
