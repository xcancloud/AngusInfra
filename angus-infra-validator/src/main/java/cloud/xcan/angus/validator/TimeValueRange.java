package cloud.xcan.angus.validator;

import cloud.xcan.angus.validator.impl.TimeValueRangeValidator;
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
@Constraint(validatedBy = {TimeValueRangeValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeValueRange {

  long MIN_TIME_VALUE = 0L;
  long MAX_TIME_VALUE = Long.MAX_VALUE;

  long minInMs() default MIN_TIME_VALUE;

  long maxInMs() default MAX_TIME_VALUE;

  String message() default "{xcan.validator.constraints.TimeValueRange.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
