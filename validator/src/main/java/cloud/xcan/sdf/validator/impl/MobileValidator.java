package cloud.xcan.sdf.validator.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.validator.Mobile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class MobileValidator implements ConstraintValidator<Mobile, String> {

  /**
   * @see cloud.xcan.sdf.core.utils.ValidatorUtil#REGEX_MOBILE
   * @see cloud.xcan.sdf.core.utils.ValidatorUtil#REGEX_MOBILE_ITUTE123
   */
  public static final String MOBILE_REGEX = "^(?:[0-9] ?){6,14}[0-9]$";

  public static final Pattern MOBILE_PATTERN = Pattern.compile(MOBILE_REGEX);

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return isNull(value) || MOBILE_PATTERN.matcher(value).matches();
  }

}
