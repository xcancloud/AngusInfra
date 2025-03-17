package io.swagger.v3.oas.models.extension;

import cloud.xcan.sdf.spec.annotations.ThirdExtension;

@ThirdExtension
public interface ExtensionKey {

  String PREFIX = "x-xc-";

  String ID = "id";
  String ID_KEY = PREFIX + ID;

  String VALUE = "value";
  String VALUE_KEY = PREFIX + VALUE;

  String TYPE = "type";
  String TYPE_KEY = PREFIX + TYPE;

  String STATUS = "status";
  String STATUS_KEY = PREFIX + STATUS;

  String ENABLED = "enabledFlag";
  String ENABLED_KEY = PREFIX + ENABLED;

  String REQUEST_SETTING = "requestSetting";
  String REQUEST_SETTING_KEY = PREFIX + REQUEST_SETTING;

  String URL = "url";
  String URL_KEY = PREFIX + URL;

  String IS_FILE = "isFile";
  String IS_FILE_KEY = PREFIX + IS_FILE;

  String FILE_NAME = "fileName";
  String FILE_NAME_KEY = PREFIX + FILE_NAME;

  String APIS_ID = "apisId";
  String APIS_ID_KEY = PREFIX + APIS_ID;

  String REQUEST_ID = "requestId";
  String REQUEST_ID_KEY = PREFIX + REQUEST_ID;

  //String SERVER_NAME = "serverName";
  //String SERVER_NAME_KEY = PREFIX + SERVER_NAME;

  String PROJECT_ID = "projectId";
  String PROJECT_ID_KEY = PREFIX + PROJECT_ID;

  String PROJECT_NAME = "projectName";
  String PROJECT_NAME_KEY = PREFIX + PROJECT_NAME;

  /**
   * Available values: {@link ApiServerSource}
   */
  String SERVER_SOURCE = "serverSource";
  String SERVER_SOURCE_KEY = PREFIX + SERVER_SOURCE;

  String MESSAGE_MODE = "messageMode";
  String MESSAGE_MODE_KEY = PREFIX + MESSAGE_MODE;

  String WS_MESSAGE = "wsMessage";
  String WS_MESSAGE_KEY = PREFIX + WS_MESSAGE;

  String CONTENT_TYPE = "contentType";
  String CONTENT_TYPE_KEY = PREFIX + CONTENT_TYPE;
  String CONTENT_ENCODING = "contentEncoding";
  String CONTENT_ENCODING_KEY = PREFIX + CONTENT_ENCODING;

  String APIKEY = "apiKey";
  String APIKEY_KEY = PREFIX + APIKEY;
  /**
   * The auth flow currently used by the apis
   */
  String OAUTH2_AUTHFLOW = "oauth2-authFlow";
  String OAUTH2_AUTHFLOW_KEY = PREFIX + OAUTH2_AUTHFLOW;
  /**
   * Flag indicating whether to use the OAuth2 process to generate a new token.
   * <p>
   * false - Use existed token, default value; true - Use auth2 flow to generate new token.
   */
  String OAUTH2_NEWTOKEN = "oauth2-newToken";
  String OAUTH2_NEWTOKEN_KEY = PREFIX + OAUTH2_NEWTOKEN;
  /**
   * Existed token.
   */
  String OAUTH2_TOKEN = "oauth2-token";
  String OAUTH2_TOKEN_KEY = PREFIX + OAUTH2_TOKEN;

  String OAUTH2_CLIENTID = "oauth2-clientId";
  String OAUTH2_CLIENTID_KEY = PREFIX + OAUTH2_CLIENTID;
  String OAUTH2_CLIENTSECRET = "oauth2-clientSecret";
  String OAUTH2_CLIENTSECRET_KEY = PREFIX + OAUTH2_CLIENTSECRET;
  String OAUTH2_CLIENTIN = "oauth2-in";
  String OAUTH2_CLIENTIN_KEY = PREFIX + OAUTH2_CLIENTIN;
  String OAUTH2_CALLBACKURL = "oauth2-callbackUrl";
  String OAUTH2_CALLBACKURL_KEY = PREFIX + OAUTH2_CALLBACKURL;
  String OAUTH2_USERNAME = "oauth2-username";
  String OAUTH2_USERNAME_KEY = PREFIX + OAUTH2_USERNAME;
  String OAUTH2_PASSWORD = "oauth2-password";
  String OAUTH2_PASSWORD_KEY = PREFIX + OAUTH2_PASSWORD;

  String EXT_AUTH_NAME = "oauth-extensionName";
  String EXT_AUTH_NAME_KEY = PREFIX + EXT_AUTH_NAME;

}
