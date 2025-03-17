
package cloud.xcan.sdf.core.biz.exception;

import static cloud.xcan.sdf.api.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.sdf.api.ExceptionLevel.ERROR;
import static cloud.xcan.sdf.api.message.CommBizException.M.BIZ_ERROR;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BizException extends AbstractResultMessageException {

  private String code;
  private String msg;
  private Object[] args;
  private Object data;

  public BizException() {
    this(BUSINESS_ERROR_CODE, BIZ_ERROR, null, ERROR);
  }

  public BizException(String code, String message) {
    this(code, message, null, null, ERROR);
  }

  public BizException(String code, String message, Throwable cause) {
    this(code, message, null, null, ERROR, cause);
  }

  public BizException(String code, String message, Object[] args, Object data) {
    this(code, message, args, data, ERROR);
  }

  public BizException(String code, String message, Object[] args, Throwable cause) {
    this(code, message, args, null, ERROR, cause);
  }

  public BizException(String code, String message, Object[] args, ExceptionLevel level) {
    this(code, message, args, null, level);
  }

  public BizException(String code, String message, Object[] args, ExceptionLevel level,
      Throwable cause) {
    this(code, message, args, null, level, cause);
  }

  public BizException(String code, String message, Object[] args, Object data,
      ExceptionLevel level) {
    this(code, message, args, data, level, null);
  }

  public BizException(String code, String message, Object[] args, Object data, Throwable cause) {
    this(code, message, args, data, null, cause);
  }

  public BizException(String code, String message, Object[] args, Object data, ExceptionLevel level,
      Throwable cause) {
    super(message, EventType.BUSINESS, level, code, cause);
    this.code = code;
    this.msg = message;
    this.args = args;
    this.data = data;
  }

  public static BizException of(String message) {
    return new BizException(BUSINESS_ERROR_CODE, message, null, ERROR);
  }

  public static BizException of(String message, Throwable cause) {
    return new BizException(BUSINESS_ERROR_CODE, message, null, ERROR, cause);
  }

  public static BizException of(String message, Object[] agrs) {
    return new BizException(BUSINESS_ERROR_CODE, message, agrs, ERROR);
  }

  public static BizException of(String message, Object[] agrs, Throwable cause) {
    return new BizException(BUSINESS_ERROR_CODE, message, agrs, ERROR, cause);
  }

  public static BizException of(String code, String message) {
    return new BizException(code, message, null, ERROR);
  }

  public static BizException of(String code, String message, Throwable cause) {
    return new BizException(code, message, null, ERROR, cause);
  }

  public static BizException of(String code, String message, Object[] agrs) {
    return new BizException(code, message, agrs, ERROR);
  }

  public static BizException of(String code, String message, Object[] agrs, Throwable cause) {
    return new BizException(code, message, agrs, ERROR, cause);
  }

  public static BizException of(String code, String message, Object[] agrs, Object data) {
    return new BizException(code, message, agrs, data, ERROR);
  }

  public static BizException of(String code, String message, Object[] agrs, Object data,
      Throwable cause) {
    return new BizException(code, message, agrs, data, ERROR, cause);
  }

  public static BizException of(String message, ExceptionLevel level) {
    return new BizException(BUSINESS_ERROR_CODE, message, null, level);
  }

  public static BizException of(String message, ExceptionLevel level, Throwable cause) {
    return new BizException(BUSINESS_ERROR_CODE, message, null, level, cause);
  }

  public static BizException of(String code, String message, ExceptionLevel level) {
    return new BizException(code, message, null, level);
  }

  public static BizException of(String message, Object[] agrs, ExceptionLevel level) {
    return new BizException(BUSINESS_ERROR_CODE, message, agrs, level);
  }

  public static BizException of(String message, Object[] agrs, ExceptionLevel level,
      Throwable cause) {
    return new BizException(BUSINESS_ERROR_CODE, message, agrs, level, cause);
  }

  public static BizException of(String code, String message, ExceptionLevel level,
      Throwable cause) {
    return new BizException(code, message, null, level, cause);
  }

  public static BizException of(String code, String message, Object[] agrs, ExceptionLevel level) {
    return new BizException(code, message, agrs, level);
  }

  public static BizException of(String code, String message, Object[] agrs, ExceptionLevel level,
      Throwable cause) {
    return new BizException(code, message, agrs, level, null, cause);
  }

  public static BizException of(String code, String message, Object[] agrs, Object data,
      ExceptionLevel level) {
    return new BizException(code, message, agrs, data, level);
  }

  public static BizException of(String code, String message, Object[] agrs, Object data,
      ExceptionLevel level, Throwable cause) {
    return new BizException(code, message, agrs, data, level, cause);
  }

  public interface M {

    String OPT_OBJ_IS_EMPTY = "xcm.optobj.empty";
    String OPT_OBJ_IS_EMPTY_T = "xcm.optobj.empty.t";
    // String OPT_OBJ_IS_NULL_KEY = "opt_obj_is_null";

    String ENABLED_DELETE_FORBIDDEN_T = "xcm.enabled.delete.forbidden.t";
  }

}
