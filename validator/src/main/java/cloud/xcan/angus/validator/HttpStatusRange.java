package cloud.xcan.angus.validator;

import cloud.xcan.angus.validator.impl.HttpStatusRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author XiaoLong Liu
 */
@Documented
@Constraint(validatedBy = {HttpStatusRangeValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpStatusRange {

  int MIN_STATUS_VALUE = 100;
  int MAX_STATUS_VALUE = 599;

  int min() default MIN_STATUS_VALUE;

  int max() default MAX_STATUS_VALUE;

  String message() default "{xcan.validator.constraints.HttpStatusRange.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
