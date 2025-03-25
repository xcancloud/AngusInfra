package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.Alpha;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class AlphaValidator extends GenericStringValidator<Alpha> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isAlpha;
  }

}
