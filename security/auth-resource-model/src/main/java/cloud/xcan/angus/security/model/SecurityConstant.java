package cloud.xcan.angus.security.model;

import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.PRINCIPAL;

public interface SecurityConstant {

  String INTROSPECTION_CLAIM_NAMES_PRINCIPAL = PRINCIPAL;
  String INTROSPECTION_CLAIM_NAMES_PERMISSION = "permissions";
  String INTROSPECTION_CLAIM_NAMES_GRANT_TYPE = "grant_type";

  String INTROSPECTION_CLAIM_NAMES_USERNAME = "username";
  String INTROSPECTION_CLAIM_NAMES_ENABLED = "enabled";
  String INTROSPECTION_CLAIM_NAMES_ACCOUNT_NON_EXPIRED = "account_non_expired";
  String INTROSPECTION_CLAIM_NAMES_ACCOUNT_NON_LOCKED = "account_non_locked";
  String INTROSPECTION_CLAIM_NAMES_CREDENTIALS_NON_EXPIRED = "credentials_non_expired";
  String INTROSPECTION_CLAIM_NAMES_ID = "id";
  String INTROSPECTION_CLAIM_NAMES_FIRST_NAME = "first_name";
  String INTROSPECTION_CLAIM_NAMES_LAST_NAME = "last_name";
  String INTROSPECTION_CLAIM_NAMES_FULL_NAME = "full_name";
  //String INTROSPECTION_CLAIM_NAMES_PASSWORD_STRENGTH = "password_strength";
  String INTROSPECTION_CLAIM_NAMES_SYS_ADMIN = "sys_admin";
  String INTROSPECTION_CLAIM_NAMES_TO_USER = "to_user";
  String INTROSPECTION_CLAIM_NAMES_MOBILE = "mobile";
  String INTROSPECTION_CLAIM_NAMES_EMAIL = "email";
  String INTROSPECTION_CLAIM_NAMES_MAIN_DEPT_ID = "main_dept_id";
  String INTROSPECTION_CLAIM_NAMES_PASSWORD_EXPIRED_DATE = "password_expired_date";
  String INTROSPECTION_CLAIM_NAMES_LAST_MODIFIED_PASSWORD_DATE = "last_modified_password_date";
  String INTROSPECTION_CLAIM_NAMES_EXPIRED_DATE = "expired_date";
  String INTROSPECTION_CLAIM_NAMES_TENANT_ID = "tenant_id";
  String INTROSPECTION_CLAIM_NAMES_TENANT_NAME = "tenant_name";
  String INTROSPECTION_CLAIM_NAMES_TENANT_REAL_NAME_STATUS = "tenant_real_name_status";
  String INTROSPECTION_CLAIM_NAMES_COUNTRY = "country";
  String INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE = "client_source";
  String INTROSPECTION_CLAIM_NAMES_DIRECTORY_ID = "directory_id";
  String INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE = "default_language";
  String INTROSPECTION_CLAIM_NAMES_DEFAULT_TIMEZONE = "default_time_zone";
  String INTROSPECTION_CLAIM_NAMES_IS_USER_TOKEN = "user_token";

  String INTROSPECTION_CLAIM_NAMES_CLIENT_ID_ISSUED_AT = "client_id_issued_at";
  String INTROSPECTION_CLAIM_NAMES_CLIENT_SECRET_EXPIRES_AT = "client_secret_expires_at";
  String INTROSPECTION_CLAIM_NAMES_CLIENT_ID = "client_id";
  String INTROSPECTION_CLAIM_NAMES_CLIENT_NAME = "client_name";
  String INTROSPECTION_CLAIM_NAMES_SCOPES = "scopes";
  String INTROSPECTION_CLAIM_NAMES_DESCRIPTION = "description";
  // String INTROSPECTION_CLAIM_NAMES_ENABLED = "enabled";
  String INTROSPECTION_CLAIM_NAMES_PLATFORM = "platform";
  //String INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE = "source";
  String INTROSPECTION_CLAIM_NAMES_BIZ_TAG = "biz_tag";
  // String INTROSPECTION_CLAIM_NAMES_TENANT_ID = "tenant_id";
  // String INTROSPECTION_CLAIM_NAMES_TENANT_NAME = "tenant_name";

  String INTROSPECTION_CLAIM_NAMES_ROOT_REQUEST_id = "request_id";
  String INTROSPECTION_CLAIM_NAMES_REQUEST_AGENT = "user_agent";
  String INTROSPECTION_CLAIM_NAMES_REQUEST_DEVICE_ID = "device_id";
  String INTROSPECTION_CLAIM_NAMES_REQUEST_REMOTE_ADDR = "remote_addr";

}
