package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.ID;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IDValidator implements ConstraintValidator<ID, Long> {

  public final static long MAX_ID_VALUE = Long.MAX_VALUE;

  @Override
  public boolean isValid(Long value, ConstraintValidatorContext context) {
    return isNull(value) || value >= 1 && value < MAX_ID_VALUE;
  }

}
