package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.UpperCase;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class UpperCaseValidator extends GenericStringValidator<UpperCase> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isAllUpperCase;
  }

}
