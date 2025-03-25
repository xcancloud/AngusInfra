package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.spec.experimental.Value;
import cloud.xcan.angus.validator.StringEnums;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class StringEnumsValidator implements ConstraintValidator<StringEnums, Value> {

  private StringEnums annotation;

  @Override
  public void initialize(StringEnums annotation) {
    this.annotation = annotation;
  }

  @Override
  public boolean isValid(Value input, ConstraintValidatorContext constraintValidatorContext) {
    if (Objects.isNull(input)) {
      return true;
    }
    boolean result = false;
    Object[] enumValues = this.annotation.enumClass().getEnumConstants();
    if (enumValues != null && enumValues.length > 0) {
      for (Object enumValue : enumValues) {
        if (enumValue instanceof Value) {
          Value ev = (Value) enumValue;
          if (input.equals(ev.getValue())) {
            result = true;
            break;
          }
        } else {
          if (input.getValue().equals(enumValue.toString())) {
            result = true;
            break;
          }
        }
      }
    }
    return result;
  }
}
