package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.Code;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.regex.Pattern;

public class CodeValidator implements ConstraintValidator<Code, String> {

  public static final String REGEX_CODE = "^[A-Za-z0-9_:\\-.]{1,80}$";
  public static final Pattern CODE_PATTERN = Pattern.compile(REGEX_CODE);

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return Objects.isNull(value) || CODE_PATTERN.matcher(value).matches();
  }

}
