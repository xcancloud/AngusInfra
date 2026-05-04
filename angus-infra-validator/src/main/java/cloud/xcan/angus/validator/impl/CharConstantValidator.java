package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.validator.CharConstant;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CharConstantValidator implements ConstraintValidator<CharConstant, String> {

  private String[] array;

  @Override
  public void initialize(CharConstant annotation) {
    this.array = annotation.array();
  }

  @Override
  public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {
    if (isEmpty(array) || isEmpty(input)) {
      return true;
    }
    for (String value : array) {
      if (value.equals(input)) {
        return true;
      }
    }
    return false;
  }

}
