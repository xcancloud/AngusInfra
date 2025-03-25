package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import cloud.xcan.angus.spec.locale.MessageHolder;
import java.util.Locale;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ReviewStatus implements ValueObject<ReviewStatus>, EnumMessage<String> {
  PENDING,
  PASSED,
  FAILED;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isPending() {
    return this.equals(PENDING);
  }

  public boolean isPassed() {
    return this.equals(PASSED);
  }

  public boolean isFailed() {
    return this.equals(FAILED);
  }

  public static ReviewStatus ofMessage(String reviewStatusMassage, Locale locale) {
    for (ReviewStatus value : values()) {
      String message = MessageHolder.message(value.getMessageKey(), locale);
      if (message.equals(reviewStatusMassage.trim())) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown test result: " + reviewStatusMassage);
  }
}
