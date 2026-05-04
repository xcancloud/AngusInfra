package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.validator.ClassField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class ClassFieldValidator implements ConstraintValidator<ClassField, String> {

  private String[] declaredFieldNames = new String[0];

  @Override
  public void initialize(ClassField constraintAnnotation) {
    Class<?> clz = constraintAnnotation.clz();
    Field[] clzFields = clz.getDeclaredFields();
    if (isEmpty(clzFields)) {
      declaredFieldNames = new String[0];
    } else {
      declaredFieldNames = new String[clzFields.length];
      for (int i = 0; i < clzFields.length; i++) {
        declaredFieldNames[i] = clzFields[i].getName();
      }
    }
  }

  @Override
  public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {
    if (isEmpty(input)) {
      return true;
    }
    if (declaredFieldNames.length == 0) {
      return true;
    }
    for (String name : declaredFieldNames) {
      if (input.equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }
}
