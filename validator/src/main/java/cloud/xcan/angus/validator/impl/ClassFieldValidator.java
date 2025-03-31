package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.validator.ClassField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class ClassFieldValidator implements ConstraintValidator<ClassField, String> {

  private Class clz;

  @Override
  public void initialize(ClassField constraintAnnotation) {
    this.clz = constraintAnnotation.clz();
  }

  @Override
  public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {
    if (isEmpty(input)) {
      return true;
    }

    Field[] clzFields = clz.getDeclaredFields();
    if (isEmpty(clzFields)) {
      return true;
    }
    for (Field clzField : clzFields) {
      if (input.equalsIgnoreCase(clzField.getName())) {
        return true;
      }
    }
    return false;
  }
}
