package cloud.xcan.sdf.validator.impl;

import cloud.xcan.sdf.validator.Blank;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class BlankValidator extends GenericStringValidator<Blank> {

  @Override
  public Function<String, Boolean> condition() {
    return StringUtils::isBlank;
  }
}
