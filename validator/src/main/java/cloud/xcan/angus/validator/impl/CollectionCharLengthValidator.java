package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.validator.CollectionCharLength;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;

public class CollectionCharLengthValidator implements
    ConstraintValidator<CollectionCharLength, Collection<String>> {

  private int maxCharLength;

  @Override
  public void initialize(CollectionCharLength annotation) {
    this.maxCharLength = annotation.maxLength();
  }

  @Override
  public boolean isValid(Collection<String> input,
      ConstraintValidatorContext constraintValidatorContext) {
    if (isNotEmpty(input)) {
      for (String i : input) {
        if (i.length() > maxCharLength) {
          return false;
        }
      }
    }
    return true;
  }

}
