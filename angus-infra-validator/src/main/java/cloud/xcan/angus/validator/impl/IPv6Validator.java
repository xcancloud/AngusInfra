package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.IPv6;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.InetAddressValidator;

public class IPv6Validator implements ConstraintValidator<IPv6, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return isNull(value) || InetAddressValidator.getInstance().isValidInet6Address(value);
  }
}
