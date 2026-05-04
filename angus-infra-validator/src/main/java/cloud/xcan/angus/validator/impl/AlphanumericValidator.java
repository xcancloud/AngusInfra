package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.Alphanumeric;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class AlphanumericValidator extends GenericStringValidator<Alphanumeric> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isAlphanumeric;
  }
}
