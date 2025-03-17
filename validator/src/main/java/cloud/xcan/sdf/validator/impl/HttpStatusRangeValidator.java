package cloud.xcan.sdf.validator.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.spec.http.HttpStatus;
import cloud.xcan.sdf.validator.HttpStatusRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HttpStatusRangeValidator implements ConstraintValidator<HttpStatusRange, HttpStatus> {

  private HttpStatusRange annotation;

  @Override
  public void initialize(HttpStatusRange annotation) {
    this.annotation = annotation;
  }

  @Override
  public boolean isValid(HttpStatus status, ConstraintValidatorContext context) {
    return isNull(status) || status.value >= annotation.min() && status.value <= annotation.max();
  }

}
