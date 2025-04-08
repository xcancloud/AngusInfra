package cloud.xcan.angus.remote.message;

import static cloud.xcan.angus.remote.ApiConstant.ECode.SYSTEM_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.URGENT;
import static cloud.xcan.angus.remote.message.SysException.M.SERVER_ERROR;
import static cloud.xcan.angus.remote.message.SysException.M.SERVER_ERROR_KEY;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.remote.ExceptionLevel;
import lombok.Getter;
import lombok.ToString;

/**
 * General System exception class, which will be handled by global exception and return http status
 * 500.
 */
@Getter
@ToString
public class SysException extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public SysException() {
    this(SYSTEM_ERROR_CODE, SERVER_ERROR, SERVER_ERROR_KEY, null, URGENT);
  }

  public SysException(String message) {
    this(SYSTEM_ERROR_CODE, message, SERVER_ERROR_KEY, null, URGENT);
  }

  public SysException(String message, Throwable cause) {
    this(SYSTEM_ERROR_CODE, message, SERVER_ERROR_KEY, null, URGENT, cause);
  }

  public SysException(String message, String eKey) {
    this(SYSTEM_ERROR_CODE, message, eKey, null, URGENT);
  }

  public SysException(String message, String eKey, Throwable cause) {
    this(SYSTEM_ERROR_CODE, message, eKey, null, URGENT, cause);
  }

  public SysException(String message, String eKey, ExceptionLevel level) {
    this(SYSTEM_ERROR_CODE, message, eKey, null, level);
  }

  public SysException(String message, String eKey, ExceptionLevel level, Throwable cause) {
    this(SYSTEM_ERROR_CODE, message, eKey, null, level, cause);
  }

  public SysException(String code, String message, Object[] args, ExceptionLevel level) {
    this(code, message, SERVER_ERROR_KEY, args, level);
  }

  public SysException(String code, String message, Object[] args, ExceptionLevel level,
      Throwable cause) {
    this(code, message, SERVER_ERROR_KEY, args, level, cause);
  }

  public SysException(String code, String message, String eKey, Object[] args,
      ExceptionLevel level) {
    this(code, message, eKey, args, level, null);
  }

  public SysException(String code, String message, String eKey, Object[] args,
      ExceptionLevel level, Throwable cause) {
    super(message, EventType.SYSTEM, level, eKey, cause);
    this.code = code;
    this.msg = message;
    this.args = args;
  }

  public static SysException of(String message) {
    return new SysException(message);
  }

  public static SysException of(String message, Throwable cause) {
    return new SysException(message, cause);
  }

  public static SysException of(String code, String message) {
    return new SysException(code, message, SERVER_ERROR_KEY, null, URGENT);
  }

  public static SysException of(String code, String message, Throwable cause) {
    return new SysException(code, message, SERVER_ERROR_KEY, null, URGENT, cause);
  }

  public static SysException of(String message, Object[] agrs) {
    return new SysException(SYSTEM_ERROR_CODE, message, SERVER_ERROR_KEY, agrs, URGENT);
  }

  public static SysException of(String message, Object[] agrs, Throwable cause) {
    return new SysException(SYSTEM_ERROR_CODE, message, SERVER_ERROR_KEY, agrs, URGENT, cause);
  }

  public static SysException of(String code, String message, String eKey) {
    return new SysException(code, message, eKey, null, URGENT);
  }

  public static SysException of(String code, String message, String eKey, Throwable cause) {
    return new SysException(code, message, eKey, null, URGENT, cause);
  }

  public static SysException of(String code, String message, String eKey, Object[] agrs) {
    return new SysException(code, message, eKey, agrs, URGENT);
  }

  public static SysException of(String code, String message, String eKey, Object[] agrs,
      Throwable cause) {
    return new SysException(code, message, eKey, agrs, URGENT, cause);
  }

  public static SysException of(String code, String message, Object[] agrs) {
    return new SysException(code, message, SERVER_ERROR_KEY, agrs, URGENT);
  }

  public static SysException of(String code, String message, Object[] agrs, Throwable cause) {
    return new SysException(code, message, SERVER_ERROR_KEY, agrs, URGENT, cause);
  }

  public static SysException of(String code, String message, Object[] agrs,
      ExceptionLevel level) {
    return new SysException(code, message, SERVER_ERROR_KEY, agrs, level);
  }

  public static SysException of(String code, String message, Object[] agrs,
      ExceptionLevel level, Throwable cause) {
    return new SysException(code, message, SERVER_ERROR_KEY, agrs, level, cause);
  }

  public static SysException of(String code, String message, ExceptionLevel level) {
    return new SysException(code, message, SERVER_ERROR_KEY, null, level);
  }

  public static SysException of(String code, String message, ExceptionLevel level,
      Throwable cause) {
    return new SysException(code, message, SERVER_ERROR_KEY, null, level, cause);
  }

  public static SysException of(ExceptionLevel level) {
    return new SysException(SYSTEM_ERROR_CODE, SERVER_ERROR_KEY, null, level);
  }

  public static SysException of(ExceptionLevel level, Throwable cause) {
    return new SysException(SYSTEM_ERROR_CODE, SERVER_ERROR_KEY, null, level, cause);
  }

  public static SysException of(String message, ExceptionLevel level) {
    return new SysException(SYSTEM_ERROR_CODE, message, SERVER_ERROR_KEY, null, level);
  }

  public static SysException of(String message, ExceptionLevel level, Throwable cause) {
    return new SysException(SYSTEM_ERROR_CODE, message, SERVER_ERROR_KEY, null, level, cause);
  }

  public static SysException of(String message, Object[] agrs, ExceptionLevel level) {
    return new SysException(SYSTEM_ERROR_CODE, message, SERVER_ERROR_KEY, agrs, level);
  }

  public static SysException of(String message, Object[] agrs, ExceptionLevel level,
      Throwable cause) {
    return new SysException(SYSTEM_ERROR_CODE, message, SERVER_ERROR_KEY, agrs, level, cause);
  }

  public static SysException of(String code, String message, String eKey, Object[] agrs,
      ExceptionLevel level) {
    return new SysException(code, message, eKey, agrs, level);
  }

  public static SysException of(String code, String message, String eKey, Object[] agrs,
      ExceptionLevel level, Throwable cause) {
    return new SysException(code, message, eKey, agrs, level, cause);
  }

  @Override
  public boolean is5xxException() {
    return true;
  }

  public interface M {

    //String SERVER_ERROR_CODE = SYSTEM_ERROR_CODE;
    String SERVER_ERROR = "xcm.server.error";
    String SERVER_ERROR_KEY = "server_error";

    String SERVICE_TIMEOUT = "xcm.service.timeout";
    String SERVICE_TIMEOUT_KEY = "service_timeout";

    String SCHEDULED_TASK_EXCEPTION = "xcm.scheduled.task.exception";
    String SCHEDULED_TASK_EXCEPTION_KEY = "scheduled_task_exception";

    String DATABASE_ACCESS_EXCEPTION = "xcm.database.access.exception";
    String DATABASE_ACCESS_EXCEPTION_KEY = "database_access_exception";

    String DATABASE_INTEGRITY_EXCEPTION = "xcm.database.integrity.exception";
    String DATABASE_INTEGRITY_EXCEPTION_KEY = "database_integrity_exception";

    String DATABASE_API_EXCEPTION = "xcm.database.api.exception";
    String DATABASE_API_EXCEPTION_KEY = "database_api_exception";

    String RPC_API_EXCEPTION = "xcm.rpc.api.exception";
    String RPC_API_EXCEPTION_KEY = "rpc_api_exception";

    String DATA_INIT_EXCEPTION = "xcm.data.init.exception";
    String DATA_INIT_EXCEPTION_T = "xcm.data.init.exception.t";
    String DATA_INIT_EXCEPTION_KEY = "data_init_exception";

    String DATA_CONFIG_EXCEPTION = "xcm.data.config.exception";
    String DATA_CONFIG_EXCEPTION_T = "xcm.data.config.exception.t";
    String DATA_CONFIG_EXCEPTION_KEY = "data_config_exception";

    String DATA_RELATIONSHIP_EXCEPTION = "xcm.data.relationship.exception";
    String DATA_RELATIONSHIP_EXCEPTION_T = "xcm.data.relationship.exception.t";
    String DATA_RELATIONSHIP_EXCEPTION_KEY = "data_relationship_exception";

    String DATA_CONFIG_NOT_FOUND = "xcm.data.config.not.found";
    String DATA_CONFIG_NOT_FOUND_T = "xcm.data.config.not.found.t";
    String DATA_CONFIG_NOT_FOUND_KEY = "data_config_not_found";

    String PRINCIPAL_MISSING = "xcm.principal.info.missing";
    String PRINCIPAL_MISSING_KEY = "principal_info_missing";

  }
}
