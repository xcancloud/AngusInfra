package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.DigitsConstant;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ObjectUtils;

public class DigitsConstantValidator implements ConstraintValidator<DigitsConstant, Integer> {

  private int[] array;

  @Override
  public void initialize(DigitsConstant annotation) {
    this.array = annotation.array();
  }

  @Override
  public boolean isValid(Integer input, ConstraintValidatorContext constraintValidatorContext) {
    if (ObjectUtils.isEmpty(array) || ObjectUtils.isEmpty(input)) {
      return true;
    }
    for (Integer value : array) {
      if (value.equals(input)) {
        return true;
      }
    }
    return false;
  }

}
