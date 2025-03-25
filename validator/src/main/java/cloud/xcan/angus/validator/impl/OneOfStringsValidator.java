package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.OneOfStrings;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class OneOfStringsValidator extends OneOfValidator<OneOfStrings, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return super.isValid(value, annotation.value(), StringUtils::equals, context);
  }
}
