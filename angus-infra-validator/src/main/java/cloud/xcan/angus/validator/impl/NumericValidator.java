package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.Numeric;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class NumericValidator extends GenericStringValidator<Numeric> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isNumeric;
  }
}
