package cloud.xcan.angus.validator.impl;

import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.angus.validator.CollectionValueNotNull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Objects;

public class CollectionValueNotNullValidator implements
    ConstraintValidator<CollectionValueNotNull, Object> {

  @Override
  public void initialize(CollectionValueNotNull annotation) {
  }

  @Override
  public boolean isValid(Object input,
      ConstraintValidatorContext constraintValidatorContext) {
    if (input instanceof Collection) {
      if (ObjectUtils.isEmpty(input)) {
        return true;
      }
      Collection values = (Collection) input;
      for (Object value : values) {
        if (Objects.isNull(value)) {
          return false;
        }
      }
    }
    return true;
  }
}
