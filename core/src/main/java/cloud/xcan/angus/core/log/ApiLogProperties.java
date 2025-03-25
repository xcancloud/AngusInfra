package cloud.xcan.angus.core.log;

import static cloud.xcan.angus.remote.ApiConstant.ApiLog.CLEAR_BEFORE_DAY;
import static cloud.xcan.angus.remote.ApiConstant.ApiLog.DEFAULT_IGNORE_PATTERN;
import static cloud.xcan.angus.remote.ApiConstant.Service.EVENT_SERVICE;
import static cloud.xcan.angus.remote.ApiConstant.Service.LOGGER_SERVICE;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.PrintLevel;
import cloud.xcan.angus.core.app.AppPropertiesRegister;
import cloud.xcan.angus.remote.ApiConstant.ApiLog;
import cloud.xcan.angus.remote.ApiConstant.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Audit log file configuration has higher priority than common service configuration.
 * <p>
 * To ensure that the common service configuration is effective, do not configure it in the
 * configuration file.
 */
@Setter
@Accessors(chain = true)
@ConfigurationProperties(prefix = "xcan.apilog", ignoreUnknownFields = false)
public class ApiLogProperties implements AppPropertiesRegister {

  // @formatter:off
  public static final String AL_ENABLED = "xcan.apilog.enabled";
  public static final String AL_LOGGER_SERVICE = "xcan.apilog.loggerService";
  public static final String AL_EVENT_SERVICE = "xcan.apilog.eventService";
  public static final String AL_CLEAR_BEFORE_DAY = "xcan.apilog.clearBeforeDay";

  @Schema(description = "Enable api log configuration")
  private Boolean enabled;

  @Schema(description = "Logger service code")
  private String loggerService;

  @Schema(description = "Event service code")
  private String eventService;

  @Schema(description = "How many days ago to clear the log, default " + CLEAR_BEFORE_DAY)
  private Integer clearBeforeDay;

  @Schema(description = "The configuration of user request log")
  private UserRequest userRequest = new UserRequest();

  @Schema(description = "The configuration of system request log")
  private SystemRequest systemRequest = new SystemRequest();

  /**
   * Default true
   */
  public Boolean getEnabled() {
    String commonConfig = System.getProperty(AL_ENABLED);
    return isNotEmpty(enabled) ? enabled : isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
  }

  /**
   * Default {@link Service#LOGGER_SERVICE}
   */
  public String getLoggerService() {
    String commonConfig = System.getProperty(AL_LOGGER_SERVICE);
    return isNotEmpty(loggerService) ? loggerService : isEmpty(commonConfig)
        ? LOGGER_SERVICE : commonConfig;
  }

  /**
   * Default {@link Service#EVENT_SERVICE}
   */
  public String getEventService() {
    String commonConfig = System.getProperty(AL_EVENT_SERVICE);
    return isNotEmpty(eventService) ? eventService : isEmpty(commonConfig)
        ? EVENT_SERVICE : commonConfig;
  }

  public Integer getClearBeforeDay() {
    String commonConfig = System.getProperty(AL_CLEAR_BEFORE_DAY);
    return nonNull(clearBeforeDay) ? clearBeforeDay : isEmpty(commonConfig)
        ? CLEAR_BEFORE_DAY : Integer.parseInt(commonConfig);
  }

  public UserRequest getUserRequest() {
    return userRequest;
  }

  public SystemRequest getSystemRequest() {
    return systemRequest;
  }

  @JsonIgnore
  @Override
  public ApiLogProperties getDefault() {
    return new ApiLogProperties().setEnabled(true)
        .setLoggerService(LOGGER_SERVICE).setEventService(EVENT_SERVICE)
        .setClearBeforeDay(CLEAR_BEFORE_DAY)
        .setUserRequest(new UserRequest().getDefault())
        .setSystemRequest(new SystemRequest().getDefault());
  }

  @Override
  public void register() {
    System.setProperty(AL_ENABLED, nonNull(getEnabled()) ? String.valueOf(getEnabled()) : "");
    System.setProperty(AL_LOGGER_SERVICE, isNotEmpty(getLoggerService()) ? getLoggerService() : "");
    System.setProperty(AL_EVENT_SERVICE, isNotEmpty(getEventService()) ? getEventService() : "");
    System.setProperty(AL_CLEAR_BEFORE_DAY, nonNull(getClearBeforeDay()) ? String.valueOf(getClearBeforeDay()) : "");

    userRequest.register();
    systemRequest.register();
  }

  @Setter
  public static class UserRequest implements AppPropertiesRegister {

    public static final String AL_UR_ENABLED = "xcan.apilog.userRequest.enabled";
    public static final String AL_UR_PRINT_LEVEL = "xcan.apilog.userRequest.printLevel";
    public static final String AL_UR_MAX_PAYLOAD_LENGTH = "xcan.apilog.userRequest.maxPayloadLength";
    public static final String AL_UR_CUSTOM_IGNORE_PATTERN = "xcan.apilog.userRequest.customIgnorePattern";
    public static final String AL_UR_PUSH_LOGGER_SERVICE = "xcan.apilog.userRequest.pushLoggerService";
    public static final String AL_UR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN = "xcan.apilog.userRequest.pushLoggerServiceIgnorePattern";

    /**
     * Record the logs of user token, /pubapi and /doorapi is also included.
     */
    @Schema(description = "Enable user access log configuration")
    private Boolean enabled;

    @Schema(description = "Log print level")
    private PrintLevel printLevel;

    @Schema(description = "Record the maximum request body size, and ignore the request body if it exceeds")
    private int maxPayloadLength;

    @Schema(description = "Ignore request urls pattern by default, value: " + DEFAULT_IGNORE_PATTERN)
    private String defaultIgnorePattern = DEFAULT_IGNORE_PATTERN;

    /**
     * Custom Ignore doc, blog, content, course in CM.
     *
     * The change of value takes effect after restarting.
     */
    @Schema(description = "Ignore request urls pattern by custom, default value is empty")
    private String customIgnorePattern;

    @Schema(description = "Push request logs to logger service, false will only save to file")
    private Boolean pushLoggerService;

    @Schema(description = "Ignore urls pattern when pushing request logs to the logger service")
    private String pushLoggerServiceIgnorePattern;

    /**
     * Default true
     */
    public Boolean getEnabled() {
      String commonConfig = System.getProperty(AL_UR_ENABLED);
      return isNotEmpty(enabled) ? enabled : isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
    }

    /**
     * Default {@link PrintLevel#HEADERS}
     */
    public PrintLevel getPrintLevel() {
      String commonConfig = System.getProperty(AL_UR_PRINT_LEVEL);
      return isNotEmpty(printLevel) ? printLevel : isEmpty(commonConfig)
          ? PrintLevel.HEADERS : PrintLevel.valueOf(commonConfig);
    }

    /**
     * Default {@link ApiLog#DEFAULT_MAX_PAYLOAD_LENGTH}
     */
    public int getMaxPayloadLength() {
      String commonConfig = System.getProperty(AL_UR_MAX_PAYLOAD_LENGTH);
      return maxPayloadLength > 0 ? maxPayloadLength : isEmpty(commonConfig)
          ? ApiLog.DEFAULT_MAX_PAYLOAD_LENGTH : Integer.parseInt(commonConfig);
    }

    public String getDefaultIgnorePattern() {
      return defaultIgnorePattern;
    }

    /**
     * Default empty
     */
    public String getCustomIgnorePattern() {
      String commonConfig = System.getProperty(AL_UR_CUSTOM_IGNORE_PATTERN);
      return isNotEmpty(customIgnorePattern) ? customIgnorePattern : isEmpty(commonConfig) ? "" : commonConfig;
    }

    /**
     * Default false
     */
    public Boolean getPushLoggerService() {
      String commonConfig = System.getProperty(AL_UR_PUSH_LOGGER_SERVICE);
      return nonNull(pushLoggerService) ? pushLoggerService : !isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
    }

    /**
     * Default empty
     */
    public String getPushLoggerServiceIgnorePattern() {
      String commonConfig = System.getProperty(AL_UR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN);
      return isNotEmpty(pushLoggerServiceIgnorePattern) ? pushLoggerServiceIgnorePattern
          : isEmpty(commonConfig) ? "" : commonConfig;
    }

    @JsonIgnore
    public String getIgnorePattern() {
      String ignorePattern  = getCustomIgnorePattern();
      return isNotEmpty(ignorePattern) ? defaultIgnorePattern + "|" + ignorePattern : defaultIgnorePattern;
    }

    @Override
    @JsonIgnore
    public UserRequest getDefault(){
      return new UserRequest().setEnabled(true).setPrintLevel(PrintLevel.HEADERS)
          .setMaxPayloadLength(ApiLog.DEFAULT_MAX_PAYLOAD_LENGTH)
          .setCustomIgnorePattern("")
          .setPushLoggerService(false).setPushLoggerServiceIgnorePattern("");
    }

    @Override
    public void register() {
      System.setProperty(AL_UR_ENABLED, nonNull(getEnabled()) ? String.valueOf(getEnabled()) : "");
      System.setProperty(AL_UR_PRINT_LEVEL, nonNull(getPrintLevel()) ? getPrintLevel().name() : "");
      System.setProperty(AL_UR_MAX_PAYLOAD_LENGTH, getMaxPayloadLength() > 0 ? String.valueOf(getMaxPayloadLength()) : "0");
      System.setProperty(AL_UR_CUSTOM_IGNORE_PATTERN, isNotEmpty(getCustomIgnorePattern()) ? getCustomIgnorePattern() : "");
      System.setProperty(AL_UR_PUSH_LOGGER_SERVICE, nonNull(getPushLoggerService()) ? String.valueOf(getPushLoggerService()) : "");
      System.setProperty(AL_UR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN, isNotEmpty(getPushLoggerServiceIgnorePattern()) ? getPushLoggerServiceIgnorePattern() : "");
    }
  }

  @Setter
  public static class SystemRequest implements AppPropertiesRegister {

    public static final String AL_SR_ENABLED = "xcan.apilog.systemRequest.enabled";
    public static final String AL_SR_PRINT_LEVEL = "xcan.apilog.systemRequest.printLevel";
    public static final String AL_SR_MAX_PAYLOAD_LENGTH = "xcan.apilog.systemRequest.maxPayloadLength";
    public static final String AL_SR_CUSTOM_IGNORE_PATTERN = "xcan.apilog.systemRequest.customIgnorePattern";
    public static final String AL_SR_PUSH_LOGGER_SERVICE = "xcan.apilog.systemRequest.pushLoggerService";
    public static final String AL_SR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN = "xcan.apilog.systemRequest.pushLoggerServiceIgnorePattern";

    /**
     * Record the logs of system token from: {@link cloud.xcan.angus.spec.experimental.BizConstant.ClientSource#XCAN_SYS_TOKEN}
     */
    @Schema(description = "Enable system access log configuration")
    private Boolean enabled;

    @Schema(description = "Log print level")
    private PrintLevel printLevel;

    @Schema(description = "Record the maximum request body size, and ignore the request body if it exceeds")
    private int maxPayloadLength;

    @Schema(description = "Ignore request urls pattern by default, value: " + DEFAULT_IGNORE_PATTERN)
    private String defaultIgnorePattern = DEFAULT_IGNORE_PATTERN;

    /**
     * Custom Ignore doc, blog, content, course in CM.
     * The change of value takes effect after restarting.
     */
    @Schema(description = "Ignore request urls pattern by custom, default value is empty")
    private String customIgnorePattern;

    @Schema(description = "Push request logs to logger service, false will only save to file")
    private Boolean pushLoggerService;

    @Schema(description = "Ignore urls pattern when pushing request logs to the logger service")
    private String pushLoggerServiceIgnorePattern;

    /**
     * Default true
     */
    public Boolean getEnabled() {
      String commonConfig = System.getProperty(AL_SR_ENABLED);
      return isNotEmpty(enabled) ? enabled : isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
    }

    /**
     * Default {@link PrintLevel#FULL}
     */
    public PrintLevel getPrintLevel() {
      String commonConfig = System.getProperty(AL_SR_PRINT_LEVEL);
      return isNotEmpty(printLevel) ? printLevel : isEmpty(commonConfig) ? PrintLevel.FULL : PrintLevel.valueOf(commonConfig);
    }

    /**
     * Default {@link ApiLog#DEFAULT_MAX_PAYLOAD_LENGTH}
     */
    public int getMaxPayloadLength() {
      String commonConfig = System.getProperty(AL_SR_MAX_PAYLOAD_LENGTH);
      return maxPayloadLength > 0 ? maxPayloadLength : isEmpty(commonConfig) ? ApiLog.DEFAULT_MAX_PAYLOAD_LENGTH : Integer.parseInt(commonConfig);
    }

    public String getDefaultIgnorePattern() {
      return defaultIgnorePattern;
    }

    /**
     * Default empty
     */
    public String getCustomIgnorePattern() {
      String commonConfig = System.getProperty(AL_SR_CUSTOM_IGNORE_PATTERN);
      return isNotEmpty(customIgnorePattern) ? customIgnorePattern : isEmpty(commonConfig) ? "": commonConfig;
    }

    /**
     * Default true
     */
    public Boolean getPushLoggerService() {
      String commonConfig = System.getProperty(AL_SR_PUSH_LOGGER_SERVICE);
      return nonNull(pushLoggerService) ? pushLoggerService : isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
    }

    /**
     * Default empty
     */
    public String getPushLoggerServiceIgnorePattern() {
      String commonConfig = System.getProperty(AL_SR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN);
      return isNotEmpty(pushLoggerServiceIgnorePattern) ? pushLoggerServiceIgnorePattern : isEmpty(commonConfig) ? "" : commonConfig;
    }

    @JsonIgnore
    public String getIgnorePattern() {
      String ignorePattern  = getCustomIgnorePattern();
      return isNotEmpty(ignorePattern) ? defaultIgnorePattern + "|" + ignorePattern : defaultIgnorePattern;
    }

    @Override
    @JsonIgnore
    public SystemRequest getDefault(){
      return new SystemRequest().setEnabled(true).setPrintLevel(PrintLevel.FULL)
          .setMaxPayloadLength(ApiLog.DEFAULT_MAX_PAYLOAD_LENGTH)
          .setCustomIgnorePattern("")
          .setPushLoggerService(true).setPushLoggerServiceIgnorePattern("");
    }

    @Override
    public void register() {
      System.setProperty(AL_SR_ENABLED, nonNull(getEnabled()) ? String.valueOf(getEnabled()) : "");
      System.setProperty(AL_SR_PRINT_LEVEL, nonNull(getPrintLevel()) ? getPrintLevel().name() : "");
      System.setProperty(AL_SR_MAX_PAYLOAD_LENGTH, getMaxPayloadLength() > 0 ? String.valueOf(getMaxPayloadLength()) : "0");
      System.setProperty(AL_SR_CUSTOM_IGNORE_PATTERN, isNotEmpty(getCustomIgnorePattern()) ? getCustomIgnorePattern() : "");
      System.setProperty(AL_SR_PUSH_LOGGER_SERVICE, nonNull(getPushLoggerService()) ? String.valueOf(getPushLoggerService()) : "");
      System.setProperty(AL_SR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN, isNotEmpty(getPushLoggerServiceIgnorePattern()) ? getPushLoggerServiceIgnorePattern() : "");
    }
  }

  // @formatter:on
}
