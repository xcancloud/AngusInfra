package cloud.xcan.angus.core.log;

import static cloud.xcan.angus.remote.ApiConstant.OperationLog.CLEAR_BEFORE_DAY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.app.AppPropertiesRegister;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author XiaoLong Liu
 */
@Setter
@Accessors(chain = true)
@ConfigurationProperties(prefix = "xcan.opt-log", ignoreUnknownFields = false)
public class OperationLogProperties implements AppPropertiesRegister {

  // @formatter:off
  public static final String OL_ENABLED = "xcan.opt-log.enabled";
  public static final String OL_CLEAR_BEFORE_DAY = "xcan.opt-log.clearBeforeDay";

  @Schema(description = "Enable operation log configuration")
  private Boolean enabled;

  @Schema(description = "How many days ago to clear the log, default " + CLEAR_BEFORE_DAY)
  private Integer clearBeforeDay;

  /**
   * Default true
   */
  public Boolean getEnabled() {
    String commonConfig = System.getProperty(OL_ENABLED);
    return isNotEmpty(enabled) ? enabled : isEmpty(commonConfig) || Boolean.parseBoolean(commonConfig);
  }

  /**
   * Default {@link cloud.xcan.angus.api.ApiConstant.OperationLog#CLEAR_BEFORE_DAY}
   */
  public Integer getClearBeforeDay() {
    String commonConfig = System.getProperty(OL_CLEAR_BEFORE_DAY);
    return nonNull(clearBeforeDay) ? clearBeforeDay
        : isEmpty(commonConfig) ? CLEAR_BEFORE_DAY : Integer.parseInt(commonConfig);
  }

  @JsonIgnore
  @Override
  public OperationLogProperties getDefault() {
    return new OperationLogProperties().setEnabled(true)
        .setClearBeforeDay(CLEAR_BEFORE_DAY);
  }

  @Override
  public void register() {
    System.setProperty(OL_ENABLED, nonNull(getEnabled()) ? String.valueOf(getEnabled()) : "");
    System.setProperty(OL_CLEAR_BEFORE_DAY, nonNull(getClearBeforeDay()) ? String.valueOf(getClearBeforeDay()) : "");
  }

  // @formatter:on
}
