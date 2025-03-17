
package cloud.xcan.sdf.validator.impl;

import cloud.xcan.sdf.validator.CharConstant;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ObjectUtils;

public class CharConstantValidator implements ConstraintValidator<CharConstant, String> {

  private String[] array;

  @Override
  public void initialize(CharConstant annotation) {
    this.array = annotation.array();
  }

  @Override
  public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {
    if (ObjectUtils.isEmpty(array) || ObjectUtils.isEmpty(input)) {
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
