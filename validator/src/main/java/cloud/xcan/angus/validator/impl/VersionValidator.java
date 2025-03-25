package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.Version;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class VersionValidator implements ConstraintValidator<Version, String> {

  public static final String VERSION_REGEX = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
  public static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return isNull(value) || VERSION_PATTERN.matcher(value).matches();
  }

}
