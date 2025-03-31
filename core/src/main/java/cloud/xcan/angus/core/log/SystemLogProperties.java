package cloud.xcan.angus.core.log;

import static cloud.xcan.angus.remote.ApiConstant.SystemLog.CLEAR_COMPRESSION_BEFORE_DAY;
import static cloud.xcan.angus.remote.ApiConstant.SystemLog.DEFAULT_COMPRESSION_MOVE_PATH;
import static cloud.xcan.angus.remote.ApiConstant.SystemLog.DEFAULT_DISK_USAGE_EXCEEDS_RATE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_FILE_PATH;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.enums.SystemLogClearWay;
import cloud.xcan.angus.core.app.AppPropertiesRegister;
import cloud.xcan.angus.remote.ApiConstant.SystemLog;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author XiaoLong Liu
 */
@Setter
@Accessors(chain = true)
@ConfigurationProperties(prefix = "xcan.syslog", ignoreUnknownFields = false)
public class SystemLogProperties implements AppPropertiesRegister {

  // @formatter:off
  // public static final String SL_ENABLED = "xcan.syslog.enabled";
  public static final String SL_CLEAR_WAY = "xcan.syslog.clearWay";
  public static final String SL_CLEAR_BEFORE_DAY = "xcan.syslog.clearBeforeDay";
  public static final String SL_COMPRESSION_BEFORE_DAY = "xcan.syslog.compressionBeforeDay";
  public static final String SL_COMPRESSION_MOVE_PATH = "xcan.syslog.compressionMovePath";
  public static final String SL_DISK_USAGE_EXCEEDS_RATE = "xcan.syslog.diskUsageExceedsRate";
  public static final String SL_DISK_USAGE_EXCEEDS_AND_CLEAR_BEFORE_DAY = "xcan.syslog.diskUsageExceedsAndClearBeforeDay";

  // System log must be enabled
  // @Schema(description = "Enable system log configuration")
  // private Boolean enabled;

  @Schema(description = "System log clearing way, default CLEAR_BEFORE_DAY", example = "CLEAR_BEFORE_DAY")
  private SystemLogClearWay clearWay;

  // Only is user behavior
  // @Schema(description = "System log auto refresh flag, default false", example = "false")
  // private Boolean autoRefreshFlag = false;

  @Schema(description = "How many days ago to clear the log, default " + CLEAR_COMPRESSION_BEFORE_DAY)
  private Integer clearBeforeDay;

  @Schema(description = "How many days ago to compress the log, default " + CLEAR_COMPRESSION_BEFORE_DAY)
  private Integer compressionBeforeDay;

  @Length(max = MAX_FILE_PATH)
  @Schema(description = "Move to position after compression, default " + DEFAULT_COMPRESSION_MOVE_PATH)
  private String compressionMovePath;

  @Schema(description = "Clean up when disk usage exceeds, default " + DEFAULT_DISK_USAGE_EXCEEDS_RATE)
  private Double diskUsageExceedsRate;

  @Schema(description = "How many days ago to clear the log when the disk usage exceeds the limit, default " + CLEAR_COMPRESSION_BEFORE_DAY)
  private Integer diskUsageExceedsAndClearBeforeDay;

  ///**
  // * Default true
  // */
  //public Boolean getEnabled() {
  //  String commonConfig = System.getProperty(SL_ENABLED);
  //  return isNotEmpty(enabled) ? enabled : isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
  //}

  /**
   * Default {@link SystemLogClearWay#CLEAR_BEFORE_DAY}
   */
  public SystemLogClearWay getClearWay() {
    String commonConfig = System.getProperty(SL_CLEAR_WAY);
    return isNotEmpty(clearWay) ? clearWay : isEmpty(commonConfig)
        ? SystemLogClearWay.CLEAR_BEFORE_DAY : SystemLogClearWay.valueOf(commonConfig);
  }

  /**
   * Default {@link SystemLog#CLEAR_COMPRESSION_BEFORE_DAY}
   */
  public Integer getClearBeforeDay() {
    String commonConfig = System.getProperty(SL_CLEAR_BEFORE_DAY);
    return nonNull(clearBeforeDay) ? clearBeforeDay
        : isEmpty(commonConfig) ? CLEAR_COMPRESSION_BEFORE_DAY : Integer.parseInt(commonConfig);
  }

  /**
   * Default {@link SystemLog#CLEAR_COMPRESSION_BEFORE_DAY}
   */
  public Integer getCompressionBeforeDay() {
    String commonConfig = System.getProperty(SL_COMPRESSION_BEFORE_DAY);
    return nonNull(compressionBeforeDay) ? compressionBeforeDay
        : isEmpty(commonConfig) ? CLEAR_COMPRESSION_BEFORE_DAY : Integer.parseInt(commonConfig);
  }

  /**
   * Default {@link SystemLog#DEFAULT_COMPRESSION_MOVE_PATH}
   */
  public String getCompressionMovePath() {
    String commonConfig = System.getProperty(SL_COMPRESSION_MOVE_PATH);
    return isNotEmpty(compressionMovePath) ? compressionMovePath
        : isEmpty(commonConfig) ? DEFAULT_COMPRESSION_MOVE_PATH : commonConfig;
  }

  /**
   * Default {@link SystemLog#DEFAULT_DISK_USAGE_EXCEEDS_RATE}
   */
  public Double getDiskUsageExceedsRate() {
    String commonConfig = System.getProperty(SL_DISK_USAGE_EXCEEDS_RATE);
    return nonNull(diskUsageExceedsRate) ? diskUsageExceedsRate : isEmpty(commonConfig)
        ? DEFAULT_DISK_USAGE_EXCEEDS_RATE : Double.parseDouble(commonConfig);
  }

  /**
   * Default {@link SystemLog#CLEAR_COMPRESSION_BEFORE_DAY}
   */
  public Integer getDiskUsageExceedsAndClearBeforeDay() {
    String commonConfig = System.getProperty(SL_DISK_USAGE_EXCEEDS_AND_CLEAR_BEFORE_DAY);
    return nonNull(diskUsageExceedsAndClearBeforeDay) ? diskUsageExceedsAndClearBeforeDay
        : isEmpty(commonConfig) ? CLEAR_COMPRESSION_BEFORE_DAY : Integer.parseInt(commonConfig);
  }

  @JsonIgnore
  @Override
  public SystemLogProperties getDefault() {
    return new SystemLogProperties().setClearWay(SystemLogClearWay.CLEAR_BEFORE_DAY)
        .setClearBeforeDay(CLEAR_COMPRESSION_BEFORE_DAY)
        .setCompressionBeforeDay(CLEAR_COMPRESSION_BEFORE_DAY)
        .setCompressionMovePath(DEFAULT_COMPRESSION_MOVE_PATH)
        .setDiskUsageExceedsRate(DEFAULT_DISK_USAGE_EXCEEDS_RATE)
        .setDiskUsageExceedsAndClearBeforeDay(CLEAR_COMPRESSION_BEFORE_DAY);
  }

  @Override
  public void register() {
    System.setProperty(SL_CLEAR_WAY, nonNull(getClearWay()) ? getClearWay().getValue() : "");
    System.setProperty(SL_CLEAR_BEFORE_DAY, nonNull(getClearBeforeDay())
        ? String.valueOf(getClearBeforeDay()) : "");
    System.setProperty(SL_COMPRESSION_BEFORE_DAY, nonNull(getCompressionBeforeDay())
        ? String.valueOf(getCompressionBeforeDay()) : "");
    System.setProperty(SL_COMPRESSION_MOVE_PATH, isNotEmpty(getCompressionMovePath())
        ? getCompressionMovePath() : "");
    System.setProperty(SL_DISK_USAGE_EXCEEDS_RATE, nonNull(getDiskUsageExceedsRate())
        ? String.valueOf(getDiskUsageExceedsRate()) : "");
    System.setProperty(SL_DISK_USAGE_EXCEEDS_AND_CLEAR_BEFORE_DAY, nonNull(getDiskUsageExceedsAndClearBeforeDay())
        ? String.valueOf(getDiskUsageExceedsAndClearBeforeDay()) : "");
  }

  // @formatter:on
}
