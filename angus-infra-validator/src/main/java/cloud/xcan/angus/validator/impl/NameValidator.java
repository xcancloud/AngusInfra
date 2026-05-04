package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.Name;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NameValidator implements ConstraintValidator<Name, String> {

  public static final String REGEX_NAME = "^[a-zA-Z0-9!@$%^&*()_\\-+=./]+$";
  public static final Pattern NAME_PATTERN = Pattern.compile(REGEX_NAME);

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return isNull(value) || NAME_PATTERN.matcher(value).matches();
  }

}
