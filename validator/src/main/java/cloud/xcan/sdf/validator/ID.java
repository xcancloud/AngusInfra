package cloud.xcan.sdf.validator;

import cloud.xcan.sdf.validator.impl.IDValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {IDValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ID {

  long max() default IDValidator.MAX_ID_VALUE;

  String message() default "{xcan.validator.constraints.ID.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
