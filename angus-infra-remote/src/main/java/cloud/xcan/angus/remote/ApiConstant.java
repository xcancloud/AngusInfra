package cloud.xcan.angus.remote;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.spec.experimental.BizConstant;

public interface ApiConstant {

  /**
   * Result code
   */
  String OK_CODE = "S";

  /**
   * Extended attributes
   */
  String EXT = "ext";

  /**
   * ErrorKey name in ext
   */
  String EXT_EKEY_NAME = "eKey";

  /**
   * ErrorKey name in ext
   */
  String EXT_CAUSE_NAME = "eKey";

  /**
   * apiKey Name
   */
  String API_KEY_NAME = "apiKey";

  /**
   * Default orderBy field
   */
  String DEFAULT_ORDER_BY = "id";

  /**
   * Default sort
   */
  OrderSort DEFAULT_ORDER_SORT = OrderSort.DESC;


  interface Service {

    String LOGGER_SERVICE_ARTIFACT_ID = BizConstant.GM_SERVICE_ARTIFACT_ID;

    String EVENT_SERVICE_ARTIFACT_ID = BizConstant.GM_SERVICE_ARTIFACT_ID;

    String COMMON_SERVICE = BizConstant.GM_SERVICE_ARTIFACT_ID;

  }

  String LOG_PATTERN = ".*/log/.*";
  String EVENT_PATTERN = ".*/event/.*";

  interface OperationLog {

    int CLEAR_BEFORE_DAY = 30;
  }

  interface ApiLog {

    /**
     * 200KB
     */
    int DEFAULT_MAX_PAYLOAD_LENGTH = 200 * 1024;
    int CLEAR_BEFORE_DAY = 15;
    String DEFAULT_IGNORE_PATTERN = ".*/webjars/.*$|.*/v2/.*$|.*/swagger.*$|.*/images/.*|.*/farvirate.ico|.*/actuator.*|.*/systemLog/.*|.*/auth/.*|.*/file/.*";
  }

  interface SystemLog {

    int CLEAR_COMPRESSION_BEFORE_DAY = 15;
    double DEFAULT_DISK_USAGE_EXCEEDS_RATE = 0.80d;
    String DEFAULT_COMPRESSION_MOVE_PATH = "/data/backup/logs";
  }

  /**
   * Error code
   */
  interface ECode {

    /**
     * Protocol exception code
     **/
    String PROTOCOL_ERROR_CODE = "E0";
    String PROTOCOL_ERROR_EVENT_CODE = "ProtocolError";
    String SECURITY_UNAUTHORIZED_EVENT_CODE = "UnauthorizedError";
    String SECURITY_FORBIDDEN_EVENT_CODE = "ForbiddenError";

    /**
     * Business failure code
     **/
    String BUSINESS_ERROR_CODE = "E1";
    String BUSINESS_ERROR_EVENT_CODE = "BusinessError";

    /**
     * System exception code
     **/
    String SYSTEM_ERROR_CODE = "E2";
    String SYSTEM_ERROR_EVENT_CODE = "SystemError";

    /**
     * Quota exception code
     **/
    String QUOTA_ERROR_CODE = "E3";
    String QUOTA_ERROR_EVENT_CODE = "QuotaError";

  }

  /**
   * Resource or validation limit
   */
  interface RLimit {

    /**
     * Default query page no
     **/
    int DEFAULT_PAGE_NO = 1;
    /**
     * Default query page size
     **/
    int DEFAULT_PAGE_SIZE = 10;
    /**
     * TThe default maximum page number of the query (mandatory)
     **/
    int MAX_PAGE_NO = 100000;
    /**
     * The default maximum page size of the query (mandatory)
     **/
    int MAX_PAGE_SIZE = 2000;
    /**
     * The default maximum number of exported records, which needs to be exported in batches
     * according to conditions(created_date) when exceeding the limit.
     */
    int MAX_REPORT_ROWS = 100000;
    /**
     * The default maximum size of the delete resource (mandatory)
     **/
    @Deprecated
    int MAX_DELETE_SIZE = MAX_BATCH_SIZE;
    /**
     * The default maximum size of the saved resource (mandatory)
     **/
    @Deprecated
    int MAX_SAVE_SIZE = MAX_BATCH_SIZE;
    /**
     * The default maximum size of the extended attributes (mandatory)
     */
    int MAX_QUERY_EXT_SIZE = 50;
    /**
     * Maximum number of advanced filtering fields
     */
    int MAX_FILTER_SIZE = 20;
    /**
     * Maximum length of filtered column name
     */
    int MAX_FILTER_COLUMN_LENGTH = 100;
  }

  /**
   * Network protocol
   */
  interface Protocol {

    String HTTP = "http";
    String HTTPS = "https";
    String WEBSOCKET = "websocket";
    String WS = "ws";
    String WSS = "wss";

  }

  String LCS_PUB = new cloud.xcan.angus.api.obf.Str0(
      new long[]{0x2CC316FF6F047A38L, 0xA711F3B372AF73E1L, 0x1F1711C68C908EB3L})
      .toString() /* => "public.key" */;

}
