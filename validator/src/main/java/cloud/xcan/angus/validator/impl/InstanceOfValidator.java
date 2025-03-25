package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.InstanceOf;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class InstanceOfValidator implements ConstraintValidator<InstanceOf, Object> {

  private InstanceOf annotation;

  @Override
  public void initialize(InstanceOf constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (Objects.isNull(value)) {
      return false;
    }

    Class<?>[] classes = annotation.value();
    for (Class<?> cls : classes) {
      if (cls.isInstance(value)) {
        return true;
      }
    }
    return false;
  }
}
