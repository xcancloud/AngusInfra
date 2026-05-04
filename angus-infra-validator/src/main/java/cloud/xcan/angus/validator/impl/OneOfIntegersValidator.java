package cloud.xcan.angus.validator.impl;


import cloud.xcan.angus.validator.OneOfIntegers;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

public class OneOfIntegersValidator extends OneOfValidator<OneOfIntegers, Integer> {

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext context) {
    return super.isValid(value, ArrayUtils.toObject(annotation.value()), Objects::equals, context);
  }
}
