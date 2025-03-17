package cloud.xcan.sdf.validator.impl;

import cloud.xcan.sdf.validator.Alpha;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class AlphaValidator extends GenericStringValidator<Alpha> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isAlpha;
  }

}
