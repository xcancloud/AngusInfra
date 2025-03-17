
package cloud.xcan.sdf.validator.impl;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import cloud.xcan.sdf.validator.CollectionCharLength;
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
    if (ObjectUtils.isNotEmpty(input)) {
      for (String i : input) {
        if (i.length() > maxCharLength) {
          return false;
        }
      }
    }
    return true;
  }

}
