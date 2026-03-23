package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.Mobile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class MobileValidator implements ConstraintValidator<Mobile, String> {

  /** Loose international-style digit groups (6–15 digits, optional spaces). */
  public static final String MOBILE_REGEX = "^(?:[0-9] ?){6,14}[0-9]$";

  public static final Pattern MOBILE_PATTERN = Pattern.compile(MOBILE_REGEX);

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return isNull(value) || MOBILE_PATTERN.matcher(value).matches();
  }

}
