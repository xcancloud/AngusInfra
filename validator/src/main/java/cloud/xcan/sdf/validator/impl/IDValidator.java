package cloud.xcan.sdf.validator.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.validator.ID;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IDValidator implements ConstraintValidator<ID, Long> {

  public final static long MAX_ID_VALUE = Long.MAX_VALUE;

  @Override
  public boolean isValid(Long value, ConstraintValidatorContext context) {
    return isNull(value) || value >= 1 && value < MAX_ID_VALUE;
  }

}
