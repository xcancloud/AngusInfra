package cloud.xcan.sdf.validator.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.spec.experimental.Value;
import cloud.xcan.sdf.validator.EnumValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

  private Class<? extends Enum<?>> enumClass;
  private boolean ignoreCase;

  @Override
  public void initialize(EnumValue constraintAnnotation) {
    this.enumClass = constraintAnnotation.enumClass();
    this.ignoreCase = constraintAnnotation.ignoreCase();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    Enum<?>[] enumConstants = enumClass.getEnumConstants();
    if (isEmpty(enumConstants)) {
      return false;
    }
    for (Enum<?> enumConstant : enumConstants) {
      if (enumConstant instanceof Value) {
        if (ignoreCase) {
          if (((Value) enumConstant).getValue().toString().equalsIgnoreCase(value)) {
            return true;
          }
        } else {
          if (((Value) enumConstant).getValue().toString().equals(value)) {
            return true;
          }
        }
      } else {
        if (ignoreCase) {
          if (enumConstant.name().equalsIgnoreCase(value)) {
            return true;
          }
        } else {
          if (enumConstant.name().equals(value)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
