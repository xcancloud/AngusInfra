package cloud.xcan.angus.validator.impl;

import static cloud.xcan.angus.spec.utils.JsonUtils.isJson;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.spec.utils.JsonPropertyExtractor;
import cloud.xcan.angus.validator.EditorContentLength;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class EditorContentLengthValidator implements
    ConstraintValidator<EditorContentLength, String> {

  private EditorContentLength annotation;

  @Override
  public void initialize(EditorContentLength annotation) {
    this.annotation = annotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (isEmpty(value)) {
      return true;
    }
    if (!isJson(value) || !annotation.value().isQuill()) {
      return value.length() <= annotation.max();
    }
    // Read quill length
    List<Object> values = JsonPropertyExtractor.extractValues(value, "insert");
    int totalLength = values.stream().map(Object::toString)
        .mapToInt(String::length).sum();
    return totalLength <= annotation.max();
  }
}
