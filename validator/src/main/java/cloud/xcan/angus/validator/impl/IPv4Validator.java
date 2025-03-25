package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.IPv4;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.InetAddressValidator;

public class IPv4Validator implements ConstraintValidator<IPv4, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return isNull(value) || InetAddressValidator.getInstance().isValidInet4Address(value);
  }
}
