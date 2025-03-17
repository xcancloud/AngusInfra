package cloud.xcan.sdf.validator.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.function.BiFunction;

public abstract class OneOfValidator<AType extends Annotation, VType> implements
    ConstraintValidator<AType, VType> {

  protected AType annotation;

  @Override
  public void initialize(AType constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  public boolean isValid(VType value, VType[] possibleValues,
      BiFunction<VType, VType, Boolean> equalsMethod, ConstraintValidatorContext context) {
    for (VType possibleVal : possibleValues) {
      if (equalsMethod.apply(value, possibleVal)) {
        return true;
      }
    }
    return false;
  }
}
