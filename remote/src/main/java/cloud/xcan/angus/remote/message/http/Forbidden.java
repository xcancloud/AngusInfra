package cloud.xcan.angus.remote.message.http;

import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.WARNING;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.FORBIDDEN;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.FORBIDDEN_KEY;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.INSUFFICIENT_PERMISSIONS_KEY;

import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.AbstractResultMessageException;
import cloud.xcan.angus.api.enums.EventType;
import lombok.Getter;
import lombok.ToString;

/**
 * Forbidden exception class, which will be handled by global exception and return http status 403.
 * <p>
 * You can continue to use the system, but some functions or interfaces are unavailable.
 */
@Getter
@ToString
public class Forbidden extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public Forbidden() {
    this(FORBIDDEN, null, FORBIDDEN_KEY, WARNING);
  }

  private Forbidden(String message, Object[] args, String eKey, ExceptionLevel level) {
    this(message, args, eKey, level, null);
  }

  private Forbidden(String message, Object[] args, String eKey, ExceptionLevel level,
      Throwable cause) {
    super(message, EventType.SECURITY, level, eKey, cause);
    this.code = PROTOCOL_ERROR_CODE;
    this.msg = message;
    this.args = args;
  }

  public static Forbidden of(String message) {
    return new Forbidden(message, null, INSUFFICIENT_PERMISSIONS_KEY, WARNING);
  }

  public static Forbidden of(String message, Throwable cause) {
    return new Forbidden(message, null, INSUFFICIENT_PERMISSIONS_KEY, WARNING, cause);
  }

  public static Forbidden of(String message, String eKey) {
    return new Forbidden(message, null, eKey, WARNING);
  }

  public static Forbidden of(String message, String eKey, Throwable cause) {
    return new Forbidden(message, null, eKey, WARNING, cause);
  }

  public static Forbidden of(ExceptionLevel level) {
    return new Forbidden(FORBIDDEN, null, INSUFFICIENT_PERMISSIONS_KEY, level);
  }

  public static Forbidden of(ExceptionLevel level, Throwable cause) {
    return new Forbidden(FORBIDDEN, null, INSUFFICIENT_PERMISSIONS_KEY, level, cause);
  }

  public static Forbidden of(String message, Object[] agrs) {
    return new Forbidden(message, agrs, INSUFFICIENT_PERMISSIONS_KEY, WARNING);
  }

  public static Forbidden of(String message, Object[] agrs, Throwable cause) {
    return new Forbidden(message, agrs, INSUFFICIENT_PERMISSIONS_KEY, WARNING, cause);
  }

  public static Forbidden of(String message, Object[] agrs, String eKey) {
    return new Forbidden(message, agrs, eKey, WARNING);
  }

  public static Forbidden of(String message, Object[] agrs, String eKey, Throwable cause) {
    return new Forbidden(message, agrs, eKey, WARNING, cause);
  }

  public static Forbidden of(String message, String eKey, ExceptionLevel level) {
    return new Forbidden(message, null, eKey, level);
  }

  public static Forbidden of(String message, String eKey, ExceptionLevel level, Throwable cause) {
    return new Forbidden(message, null, eKey, level, cause);
  }

  public static Forbidden of(String message, Object[] agrs, String eKey, ExceptionLevel level) {
    return new Forbidden(message, agrs, eKey, level);
  }

  public static Forbidden of(String message, Object[] agrs, String eKey, ExceptionLevel level,
      Throwable cause) {
    return new Forbidden(message, agrs, eKey, level, cause);
  }

  @Override
  public boolean is4xxException() {
    return true;
  }

  /**
   * When the eKey is forbidden, the front-end will forcibly exit the user.
   */
  public interface M {

    /////// fatal -> exit /////////////////////////
    String FATAL_EXIT_KEY = "fatal_exit";
    String FATAL_EXIT = "xcm.fatal.exit";
    String INVALID_ACCESS_DEVICE_ID = "xcm.invalid.access.device.id.t";
    String DENIED_OP_TENANT_ACCESS_T = "xcm.denied.op.tenant.access.t";

    /////// default /////////////////////////
    String FORBIDDEN_KEY = "forbidden";
    String FORBIDDEN = "xcm.forbidden";
    ///////////////////////////////////

    /////// forbidden -> tips and do not exit /////////
    String INSUFFICIENT_SCOPE_KEY = "insufficient_scope";
    String INSUFFICIENT_SCOPE = "xcm.insufficient.scope";

    String INSUFFICIENT_PERMISSIONS_KEY = "insufficient_permissions";
    String INSUFFICIENT_PERMISSIONS = "xcm.insufficient.permissions";

    String NO_ADMIN_PERMISSION = "xcm.no.admin.permission";
    String NO_SYS_ADMIN_PERMISSION = "xcm.no.sys.admin.permission";

    String NO_POLICY_PERMISSION_T = "xcm.no.policy.permission.t";
    String NO_OP_POLICY_PERMISSION_T = "xcm.no.op.policy.permission.t";
    String NO_TO_POLICY_PERMISSION_T = "xcm.no.to.policy.permission.t";

    String NO_OP_CLIENT_PERMISSION = "xcm.no.op.client.permission";
    String NO_TENANT_CLIENT_PERMISSION = "xcm.no.tenant.client.permission";

    String NO_CLOUD_SERVICE_EDITION_PERMISSION = "xcm.no.cloud.service.edition.permission";

    String NO_TO_USER_PERMISSION = "xcm.no.to.user.permission";

    String NO_TENANT_SYS_ADMIN_PERMISSION = "xcm.no.tenant.sys.admin.permission";
    String NO_TENANT_APP_ADMIN_PERMISSION = "xcm.no.tenant.app.admin.permission";
    String NO_OP_SYS_ADMIN_PERMISSION = "xcm.no.op.sys.admin.permission";
    String NO_OP_APP_ADMIN_PERMISSION = "xcm.no.op.app.admin.permission";

    String NO_POLICY_PERMISSION_KEY = "no_policy_permission_key";
    String NO_OP_POLICY_PERMISSION_KEY = "no_op_policy_permission_key";
    String NO_TO_POLICY_PERMISSION_KEY = "no_to_policy_permission_key";
    String NO_TO_USER_PERMISSION_KEY = "no_to_user_permission_key";

    String NO_ADMIN_PERMISSION_KEY = "no_admin_permission_key";
    /////// forbidden -> tips /////////
  }

}
