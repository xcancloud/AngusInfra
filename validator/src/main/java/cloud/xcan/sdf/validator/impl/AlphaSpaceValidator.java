package cloud.xcan.sdf.validator.impl;

import cloud.xcan.sdf.validator.AlphaSpace;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class AlphaSpaceValidator extends GenericStringValidator<AlphaSpace> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isAlphaSpace;
  }
}
