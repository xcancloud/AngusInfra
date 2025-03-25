package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.LowerCase;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class LowerCaseValidator extends GenericStringValidator<LowerCase> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isAllLowerCase;
  }
}
