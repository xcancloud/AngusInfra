package cloud.xcan.sdf.validator.impl;

import cloud.xcan.sdf.validator.OneOfDoubles;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

public class OneOfDoublesValidator extends OneOfValidator<OneOfDoubles, Double> {

  @Override
  public boolean isValid(Double value, ConstraintValidatorContext context) {
    return super.isValid(value, ArrayUtils.toObject(annotation.value()), Objects::equals, context);
  }

}
