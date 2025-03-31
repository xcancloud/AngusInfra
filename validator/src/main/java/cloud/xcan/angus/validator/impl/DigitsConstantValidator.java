package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.validator.DigitsConstant;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DigitsConstantValidator implements ConstraintValidator<DigitsConstant, Integer> {

  private int[] array;

  @Override
  public void initialize(DigitsConstant annotation) {
    this.array = annotation.array();
  }

  @Override
  public boolean isValid(Integer input, ConstraintValidatorContext constraintValidatorContext) {
    if (isEmpty(array) || isEmpty(input)) {
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
