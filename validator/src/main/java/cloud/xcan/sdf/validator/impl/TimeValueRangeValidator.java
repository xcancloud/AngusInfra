package cloud.xcan.sdf.validator.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.spec.unit.TimeValue;
import cloud.xcan.sdf.validator.TimeValueRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TimeValueRangeValidator implements ConstraintValidator<TimeValueRange, TimeValue> {

  private TimeValueRange annotation;

  @Override
  public void initialize(TimeValueRange annotation) {
    this.annotation = annotation;
  }

  @Override
  public boolean isValid(TimeValue value, ConstraintValidatorContext context) {
    return isNull(value) || value.toMilliSecond() >= annotation.minInMs()
        && value.toMilliSecond() <= annotation.maxInMs();
  }

}
