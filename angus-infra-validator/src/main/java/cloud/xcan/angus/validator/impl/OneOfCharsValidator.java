package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.OneOfChars;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

public class OneOfCharsValidator extends OneOfValidator<OneOfChars, Character> {

  @Override
  public boolean isValid(Character value, ConstraintValidatorContext context) {
    return super.isValid(value, ArrayUtils.toObject(annotation.value()), Objects::equals, context);
  }
}
