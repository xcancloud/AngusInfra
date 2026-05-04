/*
 * Copyright (c) 2021   XCan Company
 *
 *        http://www_xcan_company
 *
 * Licensed under the XCBL(XCan Business License) Version 1_0_
 * Detail XCBL license at:
 *
 * http://www_xcan_company/licenses/XCBL-1_0
 */
package cloud.xcan.angus.remote.message.http;


import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.WARNING;
import static cloud.xcan.angus.remote.message.http.Unauthorized.M.UNAUTHORIZED;
import static cloud.xcan.angus.remote.message.http.Unauthorized.M.UNAUTHORIZED_KEY;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.AbstractResultMessageException;
import lombok.Getter;
import lombok.ToString;

/**
 * Unauthorized exception class, which will be handled by global exception and return http status
 * 401.
 * <p>
 * The front-end page needs to log out and log in again_
 */
@Getter
@ToString
public class Unauthorized extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public Unauthorized() {
    this(UNAUTHORIZED, null, UNAUTHORIZED_KEY, WARNING);
  }

  private Unauthorized(String message, Object[] args, String eKey, ExceptionLevel level) {
    this(message, args, eKey, level, null);
  }

  private Unauthorized(String message, Object[] args, String eKey, ExceptionLevel level,
      Throwable cause) {
    super(message, EventType.SECURITY, level, eKey, cause);
    this.msg = message;
    this.code = PROTOCOL_ERROR_CODE;
    this.args = args;
  }

  public static Unauthorized of(String message) {
    return new Unauthorized(message, null, UNAUTHORIZED_KEY, WARNING);
  }

  public static Unauthorized of(String message, Throwable cause) {
    return new Unauthorized(message, null, UNAUTHORIZED_KEY, WARNING, cause);
  }

  public static Unauthorized of(String message, Object[] args) {
    return new Unauthorized(message, args, UNAUTHORIZED_KEY, WARNING);
  }

  public static Unauthorized of(String message, Object[] args, Throwable cause) {
    return new Unauthorized(message, args, UNAUTHORIZED_KEY, WARNING, cause);
  }

  public static Unauthorized of(String message, Object[] args, ExceptionLevel level) {
    return new Unauthorized(message, args, UNAUTHORIZED_KEY, level);
  }

  public static Unauthorized of(String message, Object[] args, ExceptionLevel level,
      Throwable cause) {
    return new Unauthorized(message, args, UNAUTHORIZED_KEY, level, cause);
  }

  public static Unauthorized of(String message, String eKey) {
    return new Unauthorized(message, null, eKey, WARNING);
  }

  public static Unauthorized of(String message, String eKey, Throwable cause) {
    return new Unauthorized(message, null, eKey, WARNING);
  }

  public static Unauthorized of(String message, Object[] args, String eKey) {
    return new Unauthorized(message, args, eKey, WARNING);
  }

  public static Unauthorized of(String message, Object[] args, String eKey, Throwable cause) {
    return new Unauthorized(message, args, eKey, WARNING, cause);
  }

  public static Unauthorized of(ExceptionLevel level) {
    return new Unauthorized(UNAUTHORIZED, null, UNAUTHORIZED_KEY, level);
  }

  public static Unauthorized of(ExceptionLevel level, Throwable cause) {
    return new Unauthorized(UNAUTHORIZED, null, UNAUTHORIZED_KEY, level, cause);
  }

  public static Unauthorized of(String message, Object[] args, String eKey, ExceptionLevel level) {
    return new Unauthorized(message, args, eKey, level);
  }

  public static Unauthorized of(String message, Object[] args, String eKey, ExceptionLevel level,
      Throwable cause) {
    return new Unauthorized(message, args, eKey, level, cause);
  }

  @Override
  public boolean is4xxException() {
    return true;
  }

  public interface M {

    String UNAUTHORIZED = "xcm.unauthorized";
    String UNAUTHORIZED_KEY = "unauthorized";

    String UNAUTHORIZED_CLIENT = "xcm.unauthorized.client";
    String UNAUTHORIZED_CLIENT_KEY = "unauthorized_client";

    String INVALID_CLIENT = "xcm.invalid.client";
    String INVALID_CLIENT_KEY = "invalid_client";

    String EXPIRED_TOKEN = "xcm.expired.token";
    String EXPIRED_TOKEN_KEY = "expired_token";

    String INVALID_TOKEN = "xcm.invalid.token";
    String INVALID_TOKEN_KEY = "invalid_token";

    String REVOKE_FAILURE = "xcm.revoke.failure";
    String REVOKE_FAILURE_KEY = "revoke_failure";

    String SIGN_VERIFY_FAILURE = "xcm.sign.verify.failure";
    String SIGN_VERIFY_FAILURE_KEY = "sign_verify_failure";

    String OPEN_AUTH_FAILURE = "xcm.open.auth.failure";
    String OPEN_AUTH_FAILURE_KEY = "open_auth_failure";

    String INVALID_PASSWORD = "xcm.invalid.password";
    String INVALID_PASSWORD_KEY = "invalid_password";

    String INVALID_ACCESS_SECRET = "xcm.invalid.access.secret";
    String INVALID_ACCESS_SECRET_KEY = "invalid_access_secret";

    String INVALID_ACCOUNT_STATUS = "invalid_account_status";

  }
}
