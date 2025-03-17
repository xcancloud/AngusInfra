package cloud.xcan.sdf.validator.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.validator.NotInstanceOf;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotInstanceOfValidator implements ConstraintValidator<NotInstanceOf, Object> {

  private NotInstanceOf annotation;

  @Override
  public void initialize(NotInstanceOf constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (isNull(value)) {
      return true;
    }
    Class<?>[] classes = annotation.value();
    for (Class<?> cls : classes) {
      if (cls.isInstance(value)) {
        return false;
      }
    }
    return true;
  }
}
