package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.AsciiPrintable;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class AsciiPrintableValidator extends GenericStringValidator<AsciiPrintable> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isAsciiPrintable;
  }
}
