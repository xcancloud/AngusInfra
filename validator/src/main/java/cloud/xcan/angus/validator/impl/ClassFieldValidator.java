package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.ClassField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import org.apache.commons.lang3.ObjectUtils;

public class ClassFieldValidator implements ConstraintValidator<ClassField, String> {

  private Class clz;

  @Override
  public void initialize(ClassField constraintAnnotation) {
    this.clz = constraintAnnotation.clz();
  }

  @Override
  public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {
    if (ObjectUtils.isEmpty(input)) {
      return true;
    }

    Field[] clzFields = clz.getDeclaredFields();
    if (ObjectUtils.isEmpty(clzFields)) {
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
