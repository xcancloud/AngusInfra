package cloud.xcan.angus.remote.message;

import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.IGNORABLE;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PROTOCOL_ERROR;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PROTOCOL_ERROR_KEY;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.remote.ExceptionLevel;
import lombok.Getter;
import lombok.ToString;

/**
 * General protocol exception class, which will be handled by global exception and return http
 * status 400.
 */
@Getter
@ToString
public class CommProtocolException extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public CommProtocolException() {
    this(PROTOCOL_ERROR, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, null, IGNORABLE);
  }

  public CommProtocolException(String message) {
    this(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, null, IGNORABLE);
  }

  public CommProtocolException(String message, Throwable cause) {
    this(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, null, IGNORABLE, cause);
  }

  public CommProtocolException(String message, String eKey) {
    this(message, EventType.PROTOCOL, eKey, null, IGNORABLE);
  }

  public CommProtocolException(String message, String eKey, Throwable cause) {
    this(message, EventType.PROTOCOL, eKey, null, IGNORABLE, cause);
  }

  public CommProtocolException(String message, String eKey, ExceptionLevel level) {
    this(message, EventType.PROTOCOL, eKey, null, level);
  }

  public CommProtocolException(String message, EventType type, String eKey,
      ExceptionLevel level, Throwable cause) {
    this(message, type, eKey, null, level, cause);
  }

  public CommProtocolException(String message, EventType type, String eKey,
      ExceptionLevel level) {
    this(message, type, eKey, null, level);
  }

  public CommProtocolException(String message, EventType type, String eKey, Object[] agrs,
      ExceptionLevel level) {
    this(message, type, eKey, agrs, level, null);
  }

  public CommProtocolException(String message, EventType type, String eKey, Object[] agrs,
      ExceptionLevel level, Throwable cause) {
    super(message, type, level, eKey, cause);
    this.code = PROTOCOL_ERROR_CODE;
    this.msg = message;
    this.args = agrs;
  }

  public static CommProtocolException of(String message) {
    return new CommProtocolException(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, null,
        IGNORABLE);
  }

  public static CommProtocolException of(String message, Throwable cause) {
    return new CommProtocolException(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, null,
        IGNORABLE, cause);
  }

  public static CommProtocolException of(String message, String eKey) {
    return new CommProtocolException(message, EventType.PROTOCOL, eKey, null, IGNORABLE);
  }

  public static CommProtocolException of(String message, String eKey, Throwable cause) {
    return new CommProtocolException(message, EventType.PROTOCOL, eKey, null, IGNORABLE, cause);
  }

  public static CommProtocolException of(ExceptionLevel level) {
    return new CommProtocolException(PROTOCOL_ERROR, EventType.PROTOCOL, PROTOCOL_ERROR_KEY,
        null, level);
  }

  public static CommProtocolException of(ExceptionLevel level, Throwable cause) {
    return new CommProtocolException(PROTOCOL_ERROR, EventType.PROTOCOL, PROTOCOL_ERROR_KEY,
        null, level, cause);
  }

  public static CommProtocolException of(String message, Object[] agrs) {
    return new CommProtocolException(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, agrs, null);
  }

  public static CommProtocolException of(String message, Object[] agrs, Throwable cause) {
    return new CommProtocolException(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, agrs, null,
        cause);
  }

  public static CommProtocolException of(String message, String eKey, Object[] agrs) {
    return new CommProtocolException(message, EventType.PROTOCOL, eKey, agrs, null);
  }

  public static CommProtocolException of(String message, String eKey, Object[] agrs,
      Throwable cause) {
    return new CommProtocolException(message, EventType.PROTOCOL, eKey, agrs, null, cause);
  }

  public static CommProtocolException of(String message, ExceptionLevel level) {
    return new CommProtocolException(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, null,
        level);
  }

  public static CommProtocolException of(String message, ExceptionLevel level, Throwable cause) {
    return new CommProtocolException(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, null,
        level, cause);
  }

  public static CommProtocolException of(String message, String eKey, ExceptionLevel level) {
    return new CommProtocolException(message, EventType.PROTOCOL, eKey, null, level);
  }

  public static CommProtocolException of(String message, Object[] agrs, ExceptionLevel level) {
    return new CommProtocolException(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, agrs,
        level);
  }

  public static CommProtocolException of(String message, Object[] agrs, ExceptionLevel level,
      Throwable cause) {
    return new CommProtocolException(message, EventType.PROTOCOL, PROTOCOL_ERROR_KEY, agrs,
        level, cause);
  }

  public static CommProtocolException of(String message, String eKey, Object[] agrs,
      ExceptionLevel level) {
    return new CommProtocolException(message, EventType.PROTOCOL, eKey, agrs, level);
  }

  public static CommProtocolException of(String message, String eKey, Object[] agrs,
      ExceptionLevel level, Throwable cause) {
    return new CommProtocolException(message, EventType.PROTOCOL, eKey, agrs, level, cause);
  }

  @Override
  public boolean is4xxException() {
    return true;
  }

  public interface M {

    String PROTOCOL_ERROR = "xcm.protocol.error";
    String PROTOCOL_ERROR_KEY = "protocol_error";

    String PROTOCOL_UNKNOWN = "xcm.protocol.unknown";
    String PROTOCOL_UNKNOWN_KEY = "protocol_unknown";

    String PARAM_ERROR = "xcm.param.error";
    String PARAM_ERROR_T = "xcm.param.error.t";
    String PARAM_ERROR_KEY = "param_error";

    String PARAM_FORMAT_ERROR_T = "xcm.param.format.error.t";
    String PARAM_FORMAT_ERROR_KEY = "param_format_error";

    String MESSAGE_FORMAT_ERROR = "xcm.message.format.error";
    String MESSAGE_FORMAT_ERROR_KEY = "message_format_error";

    String PARAM_MISSING = "xcm.param.missing";
    String PARAM_MISSING_T = "xcm.param.missing.t";
    String PARAM_MISSING_KEY = "param_missing";

    String PARAM_PARSING_ERROR = "xcm.param.parsing.error";
    String PARAM_PARSING_ERROR_KEY = "param_parsing_error";

    String EXPRESSION_PARSING_ERROR = "xcm.expression.parsing.error";
    String EXPRESSION_PARSING_ERROR_T = "xcm.expression.parsing.error.t";
    String EXPRESSION_PARSING_ERROR_KEY = "expression_parsing_error";

    String PARAM_BINDING_ERROR = "xcm.param.binding.error";
    String PARAM_BINDING_ERROR_T = "xcm.param.binding.error.t";
    String PARAM_BINDING_ERROR_T2 = "xcm.param.binding.error.t2";
    String PARAM_BINDING_ERROR_KEY = "param_binding_error";

    String PARAM_VALIDATION_ERROR = "xcm.param.validation.error";
    String PARAM_VALIDATION_ERROR_T = "xcm.param.validation.error.t";
    String PARAM_VALIDATION_ERROR_KEY = "param_validation_error";

    String PARAM_VALUE_DUPLICATE_T = "xcm.param.value.duplicate.t";
    String PARAM_VALUE_DUPLICATE_T2 = "xcm.param.value.duplicate.t2";

    String RESOURCE_ID_EMPTY = "xcm.resource.id.empty";
    String RESOURCE_ID_EMPTY_T = "xcm.resource.id.empty.t";
    String RESOURCE_ID_EMPTY_KEY = "resource_id_empty";

    String QUERY_FIELD_EMPTY = "xcm.query.field.empty";
    String QUERY_FIELD_EMPTY_T = "xcm.query.field.empty.t";
    String QUERY_FIELD_EMPTY_KEY = "query_field_empty";

    String QUERY_RANGE_TOO_LARGE = "xcm.query.range.too.large";
    String QUERY_RANGE_TOO_LARGE_KEY = "query_range_too_large";

    String UNSUPPORTED_FILTER_FIELD_T = "xcm.unsupported.filter.field.t";
    String UNSUPPORTED_FILTER_FIELD_T2 = "xcm.unsupported.filter.field.t2";
    String UNSUPPORTED_FILTER_FIELD_KEY = "unsupported_filter_field";

    String UNSUPPORTED_ORDER_BY_T = "xcm.unsupported.order.by.t";
    String UNSUPPORTED_ORDER_BY_KEY = "unsupported_order_by";

    String UNSUPPORTED_RANGE_FILTER_T = "xcm.unsupported.range.filter.t";
    String UNSUPPORTED_RANGE_FILTER_KEY = "unsupported_range_filter";

    String UNSUPPORTED_MATCH_FILTER_T = "xcm.unsupported.match.filter.t";
    String UNSUPPORTED_MATCH_FILTER_KEY = "unsupported_match_filter";

    String UNSUPPORTED_NOT_FILTER_T = "xcm.unsupported.not.filter.t";
    String UNSUPPORTED_NOT_FILTER_KEY = "unsupported_not_filter";

    String CLIENT_NOT_FOUND = "xcm.client.not.found";
    String CLIENT_NOT_FOUND_KEY = "client_not_found";

    String INVALID_GRANT = "xcm.invalid.grant";
    String INVALID_GRANT_KEY = "invalid_grant";

    String INVALID_SCOPE = "xcm.invalid.scope";
    String INVALID_SCOPE_KEY = "invalid_scope";

    String AUTHORIZATION_NOT_FOUND = "xcm.authorization.not.found";
    String AUTHORIZATION_NOT_FOUND_KEY = "authorization_not_found";

    String UNSUPPORTED_GRANT_TYPE = "xcm.unsupported.grant.type";
    String UNSUPPORTED_GRANT_TYPE_KEY = "unsupported_grant_type";

    String UNSUPPORTED_SIGNIN_TYPE = "xcm.unsupported.signin.type";
    String UNSUPPORTED_SIGNIN_TYPE_KEY = "unsupported_signin_type";

    String API_KEY_NOT_FOUND = "xcm.apikey.not.found";
    String API_KEY_NOT_FOUND_KEY = "api_key_not_found";

    String API_KEY_ERROR = "xcm.apikey.error";
    String API_KEY_ERROR_KEY = "api_key_error";

    String KEY_ID_NOT_FOUND = "xcm.keyid.not.found";
    String KEY_ID_NOT_FOUND_KEY = "key_id_not_found";

    String KEY_SECRET_NOT_FOUND = "xcm.keysecret.not.found";
    String KEY_SECRET_NOT_FOUND_KEY = "key_secret_not_found";

    String KEY_ID_SECRET_ERROR = "xcm.apikey.keysecret.error";
    String KEY_ID_SECRET_ERROR_KEY = "key_id_secret_error";

    String UNSUPPORTED_RESPONSE_TYPE = "xcm.unsupported.response.type";
    String UNSUPPORTED_RESPONSE_TYPE_KEY = "unsupported_response_type";

    String USER_DENIED_AUTHORIZATION = "xcm.user.denied.authorization";
    String USER_DENIED_AUTHORIZATION_KEY = "user_denied_authorization";

    String ACCOUNT_PASSD_ERROR = "xcm.account.passd.error";
    String ACCOUNT_PASSD_ERROR_KEY = "account_passd_error";

    String USER_DISABLED = "xcm.user.disabled";
    String USER_DISABLED_T = "xcm.user.disabled.t";
    String USER_DISABLED_KEY = "user_disable";

    String USER_LOCKED = "xcm.user.locked";
    String USER_LOCKED_T = "xcm.user.locked.t";
    String USER_LOCKED_KEY = "user_locked";

    String USER_DELETED = "xcm.user.deleted";
    String USER_DELETED_T = "xcm.user.deleted.t";
    String USER_DELETED_KEY = "user_deleted";

    String USER_EXPIRED = "xcm.user.expired";
    String USER_EXPIRED_T = "xcm.user.expired.t";
    String USER_EXPIRED_KEY = "user_expired";

    String TENANT_DELETED = "xcm.tenant.locked";
    String TENANT_DELETED_KEY = "tenant_locked";

    String CREDENTIALS_EXPIRED = "xcm.credentials.expired";
    String CREDENTIALS_EXPIRED_KEY = "credentials_expired";

    String MOBILE_FORMAT_ERROR = "xcm.mobile.format.error.t";
    String MOBILE_FORMAT_ERROR_KEY = "mobile_format_error";

    String MOBILE_NOT_EXIST = "xcm.mobile.not.exist";
    String MOBILE_NOT_EXIST_T = "xcm.mobile.not.exist.t";
    String MOBILE_NOT_EXIST_KEY = "mobile_not_exist";
    String MOBILE_EXIST_T = "xcm.mobile.exist.t";

    String EMAIL_NOT_EXIST = "xcm.email.not.exist";
    String EMAIL_NOT_EXIST_T = "xcm.email.not.exist.t";
    String EMAIL_NOT_EXIST_KEY = "email_not_exist";
    String EMAIL_EXIST_T = "xcm.email.exist.t";

  }
}
