package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.Port;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PortValidator implements ConstraintValidator<Port, Integer> {

  public final static int MIN_PORT_VALUE = 1;
  public final static int MAX_PORT_VALUE = 65535;

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext context) {
    return isNull(value) || value >= MIN_PORT_VALUE && value <= MAX_PORT_VALUE;
  }

}
