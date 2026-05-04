package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.validator.EnumPart;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EnumsPartValidator implements ConstraintValidator<EnumPart, Object> {

  private Class<? extends Enum<?>> enumClass;
  private String[] allowableValues;
  private boolean ignoreCase;

  @Override
  public void initialize(EnumPart annotation) {
    this.enumClass = annotation.enumClass();
    this.allowableValues = annotation.allowableValues();
    this.ignoreCase = annotation.ignoreCase();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
    if (Objects.isNull(value)) {
      return true;
    }
    Enum<?>[] enumConstants = this.enumClass.getEnumConstants();
    if (isEmpty(enumConstants)) {
      return false;
    }

    List<String> allowableValues0 = new ArrayList<>();
    if (isNotEmpty(allowableValues)) {
      allowableValues0 = !ignoreCase ? Arrays.asList(allowableValues) :
          Arrays.stream(allowableValues).map(String::toUpperCase).collect(Collectors.toList());
    }

    for (Enum<?> enumValue : enumConstants) {
      String stringValue = !ignoreCase ? value.toString() : value.toString().toUpperCase();
      if (stringValue.equals(/*enumValue instanceof Value ? ((Value)enumValue).getValue() : */
          enumValue.name())) {
        if (isEmpty(allowableValues0)) {
          return true;
        }
        return allowableValues0.contains(stringValue);
      }
    }
    return false;
  }

}
