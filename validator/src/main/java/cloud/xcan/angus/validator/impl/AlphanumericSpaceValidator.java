package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.AlphanumericSpace;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class AlphanumericSpaceValidator extends GenericStringValidator<AlphanumericSpace> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isAlphanumericSpace;
  }
}
