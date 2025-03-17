
package cloud.xcan.sdf.validator.impl;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import cloud.xcan.sdf.validator.MapCharLength;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Set;

public class MapCharLengthValidator implements
    ConstraintValidator<MapCharLength, Map<String, Object>> {

  private int keyMaxLength;

  private int valueMaxLength;

  @Override
  public void initialize(MapCharLength annotation) {
    this.keyMaxLength = annotation.keyMaxLength();
    this.valueMaxLength = annotation.valueMaxLength();
  }

  @Override
  public boolean isValid(Map<String, Object> input,
      ConstraintValidatorContext constraintValidatorContext) {
    if (ObjectUtils.isNotEmpty(input)) {
      Set<String> keys = input.keySet();
      for (String key : keys) {
        if (key.length() > keyMaxLength || input.get(key).toString().length() > valueMaxLength) {
          return false;
        }
      }
    }
    return true;
  }

}
