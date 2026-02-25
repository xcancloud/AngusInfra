package cloud.xcan.angus.core.log;

import static cloud.xcan.angus.core.log.ApiLogProperties.ApiRequest.SR_PRINT_LEVEL;
import static cloud.xcan.angus.remote.ApiConstant.ApiLog.CLEAR_BEFORE_DAY;
import static cloud.xcan.angus.remote.ApiConstant.ApiLog.DEFAULT_IGNORE_PATTERN;
import static cloud.xcan.angus.remote.ApiConstant.Service.EVENT_SERVICE_ARTIFACT_ID;
import static cloud.xcan.angus.remote.ApiConstant.Service.LOGGER_SERVICE_ARTIFACT_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.PrintLevel;
import cloud.xcan.angus.core.app.AppPropertiesRegister;
import cloud.xcan.angus.remote.ApiConstant.ApiLog;
import cloud.xcan.angus.remote.ApiConstant.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
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
@ConfigurationProperties(prefix = "xcan.api-log")
public class ApiLogProperties implements AppPropertiesRegister {

  // @formatter:off
  public static final String ENABLED = "xcan.api-log.enabled";
  public static final String LOGGER_SERVICE = "xcan.api-log.loggerService";
  public static final String CLEAR_BEFORE_DAY_IN_CONFIG = "xcan.api-log.clearBeforeDay";

  private Boolean enabled = true;

  private String loggerService;

  private PrintLevel printLevel = PrintLevel.FULL;

  private Integer clearBeforeDay;

  @Getter
  private ApiRequest apiRequest = new ApiRequest();

  public Boolean getEnabled() {
    String commonConfig = System.getProperty(ENABLED);
    return isNotEmpty(enabled) ? enabled : isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
  }

  /**
   * Default {@link Service#LOGGER_SERVICE_ARTIFACT_ID}
   */
  public String getLoggerService() {
    String commonConfig = System.getProperty(LOGGER_SERVICE);
    return isNotEmpty(loggerService) ? loggerService : isEmpty(commonConfig)
        ? LOGGER_SERVICE_ARTIFACT_ID : commonConfig;
  }

  /**
   * Default {@link PrintLevel#FULL}
   */
  public PrintLevel getPrintLevel() {
    String commonConfig = System.getProperty(SR_PRINT_LEVEL);
    return isNotEmpty(printLevel) ? printLevel : isEmpty(commonConfig) ? PrintLevel.FULL : PrintLevel.valueOf(commonConfig);
  }


  public Integer getClearBeforeDay() {
    String commonConfig = System.getProperty(CLEAR_BEFORE_DAY_IN_CONFIG);
    return nonNull(clearBeforeDay) ? clearBeforeDay : isEmpty(commonConfig)
        ? CLEAR_BEFORE_DAY : Integer.parseInt(commonConfig);
  }

  @JsonIgnore
  @Override
  public ApiLogProperties getDefault() {
    return new ApiLogProperties().setEnabled(true)
        .setLoggerService(LOGGER_SERVICE_ARTIFACT_ID)
        .setClearBeforeDay(CLEAR_BEFORE_DAY)
        .setApiRequest(new ApiRequest().getDefault());
  }

  @Override
  public void register() {
    System.setProperty(ENABLED, nonNull(getEnabled()) ? String.valueOf(getEnabled()) : "");
    System.setProperty(LOGGER_SERVICE, isNotEmpty(getLoggerService()) ? getLoggerService() : "");
    System.setProperty(CLEAR_BEFORE_DAY_IN_CONFIG, nonNull(getClearBeforeDay()) ? String.valueOf(getClearBeforeDay()) : "");

    apiRequest.register();
  }

  @Setter
  public static class ApiRequest implements AppPropertiesRegister {

    public static final String SR_PRINT_LEVEL = "xcan.apilog.apiRequest.printLevel";
    public static final String SR_MAX_PAYLOAD_LENGTH = "xcan.apilog.apiRequest.maxPayloadLength";
    public static final String SR_CUSTOM_IGNORE_PATTERN = "xcan.apilog.apiRequest.customIgnorePattern";
    public static final String SR_PUSH_LOGGER_SERVICE = "xcan.apilog.apiRequest.pushLoggerService";
    public static final String SR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN = "xcan.apilog.apiRequest.pushLoggerServiceIgnorePattern";

    private int maxPayloadLength;

    @Getter
    private String defaultIgnorePattern = DEFAULT_IGNORE_PATTERN;

    private String customIgnorePattern;

    private Boolean pushLoggerService;

    private String pushLoggerServiceIgnorePattern;

    /**
     * Default {@link ApiLog#DEFAULT_MAX_PAYLOAD_LENGTH}
     */
    public int getMaxPayloadLength() {
      String commonConfig = System.getProperty(SR_MAX_PAYLOAD_LENGTH);
      return maxPayloadLength > 0 ? maxPayloadLength : isEmpty(commonConfig) ? ApiLog.DEFAULT_MAX_PAYLOAD_LENGTH : Integer.parseInt(commonConfig);
    }

    public String getCustomIgnorePattern() {
      String commonConfig = System.getProperty(SR_CUSTOM_IGNORE_PATTERN);
      return isNotEmpty(customIgnorePattern) ? customIgnorePattern : isEmpty(commonConfig) ? "": commonConfig;
    }

    public Boolean getPushLoggerService() {
      String commonConfig = System.getProperty(SR_PUSH_LOGGER_SERVICE);
      return nonNull(pushLoggerService) ? pushLoggerService : isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
    }

    public String getPushLoggerServiceIgnorePattern() {
      String commonConfig = System.getProperty(SR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN);
      return isNotEmpty(pushLoggerServiceIgnorePattern) ? pushLoggerServiceIgnorePattern : isEmpty(commonConfig) ? "" : commonConfig;
    }

    @JsonIgnore
    public String getIgnorePattern() {
      String ignorePattern  = getCustomIgnorePattern();
      return isNotEmpty(ignorePattern) ? defaultIgnorePattern + "|" + ignorePattern : defaultIgnorePattern;
    }

    @Override
    @JsonIgnore
    public ApiRequest getDefault(){
      return new ApiRequest()
          .setMaxPayloadLength(ApiLog.DEFAULT_MAX_PAYLOAD_LENGTH)
          .setCustomIgnorePattern("")
          .setPushLoggerService(true).setPushLoggerServiceIgnorePattern("");
    }

    @Override
    public void register() {
      System.setProperty(SR_MAX_PAYLOAD_LENGTH, getMaxPayloadLength() > 0 ? String.valueOf(getMaxPayloadLength()) : "0");
      System.setProperty(SR_CUSTOM_IGNORE_PATTERN, isNotEmpty(getCustomIgnorePattern()) ? getCustomIgnorePattern() : "");
      System.setProperty(SR_PUSH_LOGGER_SERVICE, nonNull(getPushLoggerService()) ? String.valueOf(getPushLoggerService()) : "");
      System.setProperty(SR_PUSH_LOGGER_SERVICE_IGNORE_PATTERN, isNotEmpty(getPushLoggerServiceIgnorePattern()) ? getPushLoggerServiceIgnorePattern() : "");
    }
  }

  // @formatter:on
}
