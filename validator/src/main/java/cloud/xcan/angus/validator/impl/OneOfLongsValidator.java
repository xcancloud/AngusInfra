package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.OneOfLongs;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

public class OneOfLongsValidator extends OneOfValidator<OneOfLongs, Long> {

  @Override
  public boolean isValid(Long value, ConstraintValidatorContext context) {
    return super.isValid(value, ArrayUtils.toObject(annotation.value()), Objects::equals, context);
  }
}
