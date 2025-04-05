package cloud.xcan.angus.spec.experimental;


import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.Platform;
import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.unit.ShortTimeUnit;
import cloud.xcan.angus.spec.unit.TimeValue;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * XCan biz constant
 */
public interface BizConstant {

  String DEFAULT_LANGUAGE = "zh_CN";

  String TENANT_ID_DB_KEY = "tenant_id";

  Long OWNER_TENANT_ID = 1L;

  String IGNORED_PLUGIN_ENV_KEY = "PLUGIN_IGNORED";
  String LOADED_PLUGIN_ENV_KEY = "PLUGIN_LOADED";
  String LOADED_MATCH_PLUGIN_ENV_KEY = "PLUGIN_LOADED_MATCH";

  /**
   * @see Platform#XCAN_TP
   */
  String XCAN_TENANT_PLATFORM_CODE = new Str0(new long[]{0x1D59B3C91B9DB6ADL, 0x63A0A495A05827B7L})
      .toString() /* => "xcan_tp" */;
  /**
   * @see Platform#XCAN_OP
   */
  String XCAN_OPERATION_PLATFORM_CODE = new Str0(
      new long[]{0xD02B78B2A482A10DL, 0x22FD2ECB43B53D4AL}).toString() /* => "xcan_op" */;
  /**
   * @see Platform#XCAN_2P
   */
  String XCAN_2P_PLATFORM_CODE = new Str0(new long[]{0xC954E9B703A0EF5BL, 0x546DE9328C9EDF81L})
      .toString() /* => "xcan_2p" */;
  /**
   * @see Platform#XCAN_3RD
   */
  String XCAN_3RD_PLATFORM_CODE = new Str0(new long[]{0xE05F58EA396C2D50L, 0x1F3CB9F441E5C9A9L})
      .toString() /* => "xcan_3rd" */;

  /**
   * Enum i18n message prefix
   */
  String ENUM_MESSAGE_PREFIX = "xcm.enum.";

  /**
   * Value i18n message prefix
   */
  String VALUE_MESSAGE_PREFIX = "xcm.value.";

  /**
   * Service artifactId
   */
  String GM_SERVICE = "AngusGM";
  String GM_SERVICE_PRIVATIZATION = "ANGUSGM";
  String TESTER_SERVICE = "XCAN-ANGUSTESTER.BOOT";
  String TESTER_SERVICE_PRIVATIZATION = "ANGUSTESTER";
  String CTRL_SERVICE = "XCAN-ANGUSCTRL.BOOT";
  String CTRL_SERVICE_PRIVATIZATION = "ANGUSCTRL";

  /**
   * String is null in database
   */
  String EMPTY_STR = "";

  /**
   * Default id of tree node parent ID
   */
  long DEFAULT_ROOT_PID = -1L;

  /**
   * Number is null in database
   */
  Long EMPTY_NUMBER = -1L;

  String DEFAULT_RESOURCE_ID = "id";
  String DEFAULT_RESOURCE_NAME = "name";
  String DEFAULT_TEXT_SEARCH_COLUMN = "extSearchMerge";

  int DEFAULT_SEQUENCE = 10000;

  int INITIAL_HASH = 7;
  int MULTIPLIER = 31;

  String EMPTY_STRING = "";
  String NULL_STRING = "null";
  String ARRAY_START = "{";
  String ARRAY_END = "}";
  String EMPTY_ARRAY = ARRAY_START + ARRAY_END;
  String ARRAY_ELEMENT_SEPARATOR = ", ";
  Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  int DEFAULT_READ_LINE_NUM = 5000;

  int DEFAULT_PRIORITY = 1000;
  int MIN_PRIORITY = 1;
  int MAX_PRIORITY = Integer.MAX_VALUE;

  String LOCK_FILE_SUFFIX = ".lock";

  /********************Param validation***********************/
  int MAX_RELATION_QUOTA = 2000;

  int DEFAULT_PARAM_SIZE = 100;
  int DEFAULT_BATCH_SIZE = 200;

  int DEFAULT_ID_LENGTH = 20;
  int DEFAULT_BID_LENGTH = 20; // Code or No length
  int DEFAULT_CODE_LENGTH = 80;
  int DEFAULT_CODE_LENGTH_X2 = 160;
  int DEFAULT_CODE_LENGTH_X5 = 400;

  int DEFAULT_NAME_LENGTH = 100;
  int DEFAULT_NAME_LENGTH_X2 = 200;
  int DEFAULT_NAME_LENGTH_X4 = 400;

  int DEFAULT_KEY_LENGTH = 40;
  int DEFAULT_KEY_LENGTH_X2 = 80;
  int DEFAULT_KEY_LENGTH_X4 = 160;

  int DEFAULT_DESC_LENGTH = 200;
  int DEFAULT_DESC_LENGTH_X2 = 400;
  int DEFAULT_DESC_LENGTH_X4 = 800;
  int DEFAULT_DESC_LENGTH_X10 = 2000;

  int DEFAULT_OUT_ID_LENGTH = 80;

  int DEFAULT_REMARK_LENGTH = 200;
  int DEFAULT_REMARK_LENGTH_X2 = 400;
  int DEFAULT_REMARK_LENGTH_X4 = 800;
  int DEFAULT_REMARK_LENGTH_X10 = 2000;
  int DEFAULT_REMARK_LENGTH_X15 = 3000;
  int DEFAULT_REMARK_LENGTH_X30 = 6000;

  int MAX_OPENAPI_PATH_NUM = 5000;
  int MAX_OPENAPI_TAG_NUM = 500;
  int MAX_OPENAPI_NAME_LENGTH = 200;
  int MAX_OPENAPI_SUMMARY_LENGTH = 400;
  int MAX_OPENAPI_DOC_DESC_LENGTH = 20000;
  int MAX_OPENAPI_OTHER_DESC_LENGTH = 5000;
  int MAX_OPENAPI_LENGTH = 5000000; // Approximately 10 MB

  int DEFAULT_ADDRESS_LENGTH = 200;

  int DEFAULT_URL_LENGTH = 200;
  int DEFAULT_URL_LENGTH_X2 = 400;
  int DEFAULT_URL_LENGTH_X4 = 800;
  int DEFAULT_URL_LENGTH_X20 = 2000;

  int DEFAULT_URI_LENGTH = 200;
  int DEFAULT_URI_LENGTH_X2 = 400;
  int DEFAULT_URI_LENGTH_X4 = 800;
  int DEFAULT_URI_LENGTH_X20 = 2000;

  int MAX_MOBILE_LENGTH = 16;
  int MAX_EMAIL_LENGTH = 100;
  int MAX_LANDLINE_LENGTH = 40;
  int MAX_ITC_LENGTH = 8;
  int MAX_COUNTRY_LENGTH = 16;
  int MAX_VERIFICATION_CODE_LENGTH = 8;

  int MAX_HTTP_AUTH_PARAM_NUM = 10;

  /**
   * @see SupportedLanguage#getValue() -> length
   */
  int MAX_LANGUAGE_LENGTH = 8;

  int MAX_FILE_NAME = 400;
  int MAX_FILE_PATH = 800;
  int MAX_VARCHAR_LENGTH = 6000;
  int MAX_ATTACHMENT_NUM = 5;
  int MAX_ATTACHMENT_NUM_X2 = 10;

  int MAX_PUBLIC_TOKEN_LENGTH = 40;
  int MAX_LINK_SECRET_LENGTH = 80;
  int MAX_CLIENT_SECRET_LENGTH = 200;
  int MAX_CLIENT_AUTHORITY_SIZE = 2000;

  int IPV4_LENGTH = 15;
  int MAX_URL_LENGTH = 6000;
  int MAX_DOMAIN_LENGTH = 200;
  int MAX_HOST_LENGTH = 200;
  @Deprecated // TODO renaming
  int MAX_TARGET_LENGTH = 50;

  int MAX_CONDITION_EXPRESSION_LENGTH = 400;

  int MAX_PARAM_NAME_LENGTH = 400;
  //int MAX_HEADER_NAME_LENGTH = 1024 * 2;

  int DEFAULT_PARAM_VALUE_LENGTH = 1024;
  int MAX_PARAM_VALUE_LENGTH = 1024 * 4;
  int MAX_PARAM_VALUE_LENGTH_X2 = MAX_PARAM_VALUE_LENGTH * 2;
  int MAX_PARAM_VALUE_LENGTH_X5 = MAX_PARAM_VALUE_LENGTH * 5;
  int MAX_PARAM_VALUE_LENGTH_X10 = MAX_PARAM_VALUE_LENGTH * 10;
  int MAX_PARAM_TYPE_LENGTH = 20;
  int MAX_PARAM_CONTENT_LENGTH = 1024 * 1024 * 2;
  int MAX_PARAM_SIZE = 200;
  int MAX_PARAM_MATCH_SIZE = 10;
  int MAX_PARAM_MATCH_SIZE_X2 = 20;
  int MAX_PARAM_MATCH_SIZE_X5 = 50;
  int MAX_PARAM_MATCH_SIZE_X10 = 100;

  int MAX_MATCH_RESPONSE_NUM = 50;

  int MAX_DESC_LENGTH = 200;
  int MAX_BIZ_KEY_LENGTH = 80;

  int MAX_CONTENT_TYPE_LENGTH = 100;

  int MAX_HTTP_METHOD_LENGTH = 8;

  int MAX_SHARE_OBJECT_NUM = 2000;
  int MAX_SHARE_PWD_LENGTH = 40;
  int MIN_SHARE_PWD_LENGTH = 6;

  /**
   * The privatization version is limited to a maximum of 20MB, and the cloud service version is
   * limited to a maximum of 2GB.
   */
  long MAX_HTTP_RAW_BODY_SIZE = 2 * 1024 * 1024 * 1024L;
  int DEFAULT_HTTP_RAW_BODY_SIZE = 20 * 1024 * 1024;
  long MAX_HTTP_FORM_VALUE_SIZE = 2 * 1024 * 1024 * 1024L;
  int DEFAULT_HTTP_FORM_VALUE_SIZE = 20 * 1024 * 1024;

  int MAX_REQUEST_RETRIES = 6;
  int MAX_REQUEST_REDIRECTS = 10;
  int MAX_REQUEST_RETRY_INTERVAL_MIN = 30;
  int MAX_REQUEST_RETRY_INTERVAL_MS = MAX_REQUEST_RETRY_INTERVAL_MIN * 60 * 1000;
  int DEFAULT_CONNECT_TIMEOUT_SECOND = 6;
  int DEFAULT_READ_TIMEOUT_SECOND = 60;
  int DEFAULT_RETRY_INTERVAL_MS = 200;
  int DEFAULT_REQUEST_RETRIES = 0;
  int DEFAULT_REQUEST_REDIRECTS = 1;

  TimeValue DEFAULT_CONNECT_TIMEOUT = TimeValue
      .of(DEFAULT_CONNECT_TIMEOUT_SECOND, ShortTimeUnit.Second);
  TimeValue DEFAULT_READ_TIMEOUT = TimeValue.of(DEFAULT_READ_TIMEOUT_SECOND, ShortTimeUnit.Second);
  TimeValue DEFAULT_RETRY_INTERVAL = TimeValue
      .of(DEFAULT_RETRY_INTERVAL_MS, ShortTimeUnit.Millisecond);

  int MAX_HEADER_AUTH_MATCH_NUM = 5;

  int MAX_APIS_URI_LENGTH = 60000;

  int ANGUS_SCRIPT_LENGTH = 1024 * 1024 * 10;

  int MAX_WORKLOAD_NUM = 10000;

  /********************Param validation***********************/


  int DEFAULT_AGENT_PORT = 6807;
  int DEFAULT_EXCHANGE_SERVER_PORT = 5035;

  // Note: Used by angus and installer project
  String START_APP_SUCCESS_MESSAGE = "Application started successfully";
  String START_RUNNER_SUCCESS_MESSAGE = "Start runner successfully";
  String START_AGENT_SUCCESS_MESSAGE = "Start agent successfully";
  String START_PROXY_SUCCESS_MESSAGE = "Start proxy successfully";
  String START_MOCK_SERVER_SUCCESS_MESSAGE = "Start mock server successfully";

  /**
   * Default date
   */
  String DEFAULT_DATE_STR = "2001-01-01 00:00:00";
  LocalDateTime DEFAULT_DATE = LocalDateTime.of(2001, 1, 1, 0, 0, 0);

  String[] AUTH_RESOURCES = {
      "/api/**",
      "/openapi2p/**",
      "/view/**"
  };

  String[] AUTH_RESOURCES_IN_FILTER = {
      "/api/*",
      "/openapi2p/*"
  };

  String[] OPEN_AUTH_RESOURCES = {
      "/openapi/**"
  };

  String[] PUBLIC_RESOURCES = {
      "/pubapi/**",
      "/pubview/**"
  };

  String[] DOOR_RESOURCES = {
      "/doorapi/**"
  };

  String[] AUTH_WHITELIST = {
      // springboot actuator
      "/favicon.ico",
      "/actuator/**",
      // angus apis
      "/pubapi/**",
      "/doorapi/**",
      //"/openapi2p/**",
      "/pubview/**",
      // "/ws/**", // Internal authentication of access tokens
      // -- swagger ui
      "/swagger-ui.html",
      "/swagger-ui/**",
      "/v3/api-docs/**",
      //"/webjars/**",
      // oauth public endpoint
      //"/oauth/user/login", "/oauth/authorize"
      "/oauth/token"
  };

  /**
   * Authentication parameter key
   */
  interface AuthKey {

    String SIGNUP_TYPE = "signup_type";

    String GRANT_TYPE = "grant_type";
    String GRANT_TYPE_HUMP = "grantType";
    String AUTHORIZATION = "Authorization";
    String USERNAME = "username";
    String EMAIL = "email";
    String MOBILE = "mobile";
    String PASSD = new Str0(new long[]{0x44ED27B90DF8C74AL, 0x26693839EEDEAA2BL})
        .toString() /* => "password" */;
    String CLIENT_ID = "client_id";
    String CLIENT_NAME = "client_name";
    String CLIENT_SECRET = new Str0(
        new long[]{0x7A2E43F1E76B6153L, 0xFC45C7301C3CAB2FL, 0x554D36C13AEC2F9L})
        .toString() /* => "client_secret" */;
    String CLIENT_SOURCE = "client_source";
    String SCOPE = "scope";
    String DEFAULT_SCOPE = "trust";
    String REFRESH_TOKEN = "refresh_token";
    String ACCESS_TOKEN = "access_token";
    String SMS_CODE = "sms_code";

    String TENANT_ID = "tenant_id";
    String TENANT_NAME = "tenant_name";
    String USER_ID = "user_id";
    String FULLNAME = "fullname";

    /**
     * Tenant application function authorization strategy prefix.
     */
    String POLICY_PREFIX = "ROLE_";
    /**
     * Operation tenant authorization role prefix.
     */
    String ROLE_TOP_PREFIX = "ROLE_TOP_";

    String HTTP_AUTH_HEADER = "Authorization";
    String BEARER_TOKEN_TYPE = "Bearer";
    String BASIC_TYPE = "Basic";

    String REQUEST_ID = "requestId";

    String AUTH_SERVICE_CODE = "serviceCode";
    String DEFAULT_AUTH_SERVICE_CODE = "AngusGM";

    String KEY_ID = "keyId";
    String KEY_SECRET = "keySecret";

    String PRINCIPAL = "principal";

    String AUTHORITY = "authorities";

    String REQUEST_PARAMETERS = "requestParameters";

    String OAUTH2_REQUEST = "oauth2Request";

    String DEFAULT_LANGUAGE = "default_language";
    String DEFAULT_LANGUAGE_HUMP = "defaultLanguage";

    String DEFAULT_TIME_ZONE = "default_time_zone";
    String DEFAULT_TIME_ZONE_HUMP = "defaultTimeZone";

    String CLIENT_ID_HUMP = "clientId";
    String CLIENT_SECRET_HUMP = "clientSecret";
    String CLIENT_SOURCE_HUMP = "clientSource";

    /**
     * The name of the token.
     */
    String BEARER = "Bearer";

    String SECURITY_SCHEME_USER_HTTP_NAME = "UserHttpBearer";
    String SECURITY_SCHEME_USER_OAUTH2_NAME = "UserOAuth2Bearer";

    String SECURITY_SCHEME_SYS_HTTP_NAME = "SysHttpBearer";
    String SECURITY_SCHEME_SYS_OAUTH2_NAME = "SysOAuth2Bearer";

    String CUSTOM_ACCESS_TOKEN = "customAccessToken";
    String ACCESS_TOKEN_EXPIRED_DATE = "accessTokenExpiredDate";

    Duration MAX_TOKEN_VALIDITY_PERIOD = Duration.ofDays(50 * 365);
  }

  /**
   * @see ClientSource In AAS Servie
   */
  interface ClientSource {

    String XCAN_TP_SIGNIN = "XCAN_TP_SIGNIN";
    String XCAN_OP_SIGNIN = "XCAN_OP_SIGNIN";
    String XCAN_USER_TOKEN = "XCAN_USER_TOKEN";
    String XCAN_SYS_TOKEN = "XCAN_SYS_TOKEN";
    String XCAN_2P_SIGNIN = "XCAN_2P_SIGNIN";
  }

  static boolean isUserSignInToken(String clientSource) {
    return nonNull(clientSource) && (clientSource.equals(ClientSource.XCAN_TP_SIGNIN)
        || clientSource.equals(ClientSource.XCAN_OP_SIGNIN)
        /*|| clientSource.equals(ClientSource.XCAN_2P_SIGNIN) -> System sign in */);
  }

  /**
   * User parameter key
   */
  interface UserKey {

    String USER_ID = "userId";

    String USER_DATA_ID = "id";

    String USER_NAME = "fullname";

    String USERNAME = "username";

    String TENANT_ID = "tenantId";

    String TENANT_NAME = "tenantName";

    String USER_TYPE = "type";

    String MAIN_DEPT_ID = "mainDeptId";

    String SYS_ADMIN_FLAG = "sysAdminFlag";

    String TO_USER_FLAG = "toUserFlag";

    String ITC = "itc";

    String COUNTRY = "country";

    String DEVICE_ID = "deviceId";

  }

  /**
   * HTTP protocol header
   */
  interface Header {

    String COOKIE = "Cookie";

    String USER_AGENT = "User-Agent";

    String CORS_CREDENTIALS = "Access-Control-Allow-Credentials";
    String CORS_ORIGIN = "Access-Control-Allow-Origin";
    String CORS_HEADERS = "Access-Control-Allow-Headers";
    String CORS_METHODS = "Access-Control-Allow-Methods";
    String CORS_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    /**
     * Authentication header
     */
    String AUTHORIZATION = "Authorization";
    String GRANT_TYPE = "grant_type";
    //    String USERNAME = "username";
    String PASSD = "password";
    String CLIENT_ID = "client_id";
    String CLIENT_SOURCE = "client_source";
    String CLIENT_SECRET = "client_secret";
    String SCOPE = "scope";
    String REFRESH_TOKEN = "refresh_token";
    String ACCESS_TOKEN = "access_token";

    /**
     * XCan custom header
     */
    String HEADER_PREFIX = "XC-";

    String NGINX_PROXY_CORS = HEADER_PREFIX + "Nginx-Proxy-Cors";

    String AUTH_CLIENT_ID = HEADER_PREFIX + "Auth-Client-Id";
    String AUTH_TENANT_ID = HEADER_PREFIX + "Auth-Tenant-Id";
    String AUTH_USER_ID = HEADER_PREFIX + "Auth-User-Id";
    String AUTH_DEVICE_ID = HEADER_PREFIX + "Auth-Device-Id";

    String SIGNUP_TYPE = HEADER_PREFIX + "Signup-Type";
    String SERVICE_ID = HEADER_PREFIX + "Service-Id";
    String INVOKE_SERVICE_ID = HEADER_PREFIX + "Invoke-Service-Id";
    String INSTANCE_ID = HEADER_PREFIX + "Instance-Id";
    String INVOKE_INSTANCE_ID = HEADER_PREFIX + "Invoke-Instance-Id";
    String RESOURCE_CODE = HEADER_PREFIX + "Resource-Code";

    String DATA_SCOPE = HEADER_PREFIX + "Data-Scope";
    String ACCESS_SCOPE = HEADER_PREFIX + "Access-Scope";

    String LANGUAGE = HEADER_PREFIX + "DEFAULT-LANGUAGE";
    String TIME_ZONE = HEADER_PREFIX + "DEFAULT-TIME-ZONE";

    String REQUEST_ID = HEADER_PREFIX + "Request-Id";
    String REQUEST_ACCEPT_TIME = HEADER_PREFIX + "Request-Accept-time";

    String SAMPLE_ID = HEADER_PREFIX + "Sample-Id";

    String TENANT_ID = HEADER_PREFIX + "Tenant-Id";
    String TENANT_NAME = HEADER_PREFIX + "Tenant-Name";

    String USER_ID = HEADER_PREFIX + "User-Id";
    String USER_FULLNAME = HEADER_PREFIX + "User-Fullname";
    String USERNAME = HEADER_PREFIX + "Username";

    String DEPT_ID = HEADER_PREFIX + "Dept-Id";
    String DEPT_SYS_ID = HEADER_PREFIX + "Dept-Sys-Id";

    String ITC = HEADER_PREFIX + "Itc";

    String COUNTRY = HEADER_PREFIX + "Country";

    String DEVICE_ID = HEADER_PREFIX + "DeviceId";

    String SYS_ADMIN_FLAG = HEADER_PREFIX + "Sys-Admin-Flag";

    String TO_USER_FLAG = HEADER_PREFIX + "To-User-Flag";

    String E_KEY = HEADER_PREFIX + "E-Key";

    @Deprecated
    String ROLES = HEADER_PREFIX + "Roles";
    @Deprecated
    String GROUPS = HEADER_PREFIX + "Groups";
    @Deprecated
    String AUTHORITIES = HEADER_PREFIX + "Authorities";

    String API_KEY = /*HEADER_PREFIX +*/ "X-API-Key";

    String OPT_TENANT_ID = HEADER_PREFIX + "Opt-Tenant-Id";
  }

  interface AppDir {

    String HOME_DIR = "HOME_DIR";
    String LIB_DIR = "LIB_DIR";
    String LIB_DIR_NAME = "lib";
    String TMP_DIR = "TMP_DIR";
    String TMP_DIR_NAME = "tmp";
    String WORK_DIR = "WORK_DIR";
    String WORK_DIR_NAME = "work";
    String DATA_DIR = "DATA_DIR";
    String DATA_DIR_NAME = "data";
    String LOGS_DIR = "LOGS_DIR";
    String LOGS_DIR_NAME = "logs";
    String CONFIG_DIR = "CONF_DIR";
    String CONFIG_DIR_NAME = "conf";
    String PLUGINS_DIR = "PLUGIN_DIR";
    String PLUGINS_DIR_NAME = "plugins";
    String SUB_APP_DIR = "SUB_APP_DIR";
    String SUB_APP_DIR_NAME = "apps";
    String STATICS_DIR = "STATICS_DIR";
    String STATICS_DIR_NAME = "statics";
  }
}
