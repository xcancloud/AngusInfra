package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.Domain;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class DomainValidator implements ConstraintValidator<Domain, String> {

  public static final String REGEX_DOMAIN = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
  public static final Pattern DOMAIN_PATTERN = Pattern.compile(REGEX_DOMAIN);

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return isNull(value) || DOMAIN_PATTERN.matcher(value).matches();
  }

}
