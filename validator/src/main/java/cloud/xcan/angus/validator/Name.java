package cloud.xcan.angus.validator;

import cloud.xcan.angus.validator.impl.NameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {NameValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {

  String message() default "{xcan.validator.constraints.name.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
