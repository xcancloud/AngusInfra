package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.validator.Domain;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * @see <a
 * href="https://stackoverflow.com/questions/106179/regular-expression-to-match-dns-hostname-or-ip-address">regular-expression-to-match-dns-hostname-or-ip-address</a>
 * @see <a href="http://en.wikipedia.org/wiki/Hostname">http://en.wikipedia.org/wiki/Hostname</a>
 */
public class HostnameValidator implements ConstraintValidator<Domain, String> {

  public static final String REGEX_HOSTNAME = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";

  public static final Pattern HOSTNAME_PATTERN = Pattern.compile(REGEX_HOSTNAME);

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return isNull(value) || HOSTNAME_PATTERN.matcher(value).matches();
  }

}
