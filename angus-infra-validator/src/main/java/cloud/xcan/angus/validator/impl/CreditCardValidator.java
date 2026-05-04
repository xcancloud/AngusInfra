package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.validator.CreditCard;
import cloud.xcan.angus.validator.CreditCardType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CreditCardValidator implements ConstraintValidator<CreditCard, String> {

  private CreditCard annotation;

  @Override
  public void initialize(CreditCard constraintAnnotation) {
    this.annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (null == value) {
      return false;
    }

    CreditCardType[] types = annotation.value();
    for (CreditCardType creditCardType : types) {
      org.apache.commons.validator.routines.CreditCardValidator ccv =
          new org.apache.commons.validator.routines.CreditCardValidator(
              creditCardType.getInternalValue());
      if (ccv.isValid(value)) {
        return true;
      }
    }
    return false;
  }
}
