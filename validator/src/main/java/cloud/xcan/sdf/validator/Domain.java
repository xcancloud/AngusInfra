package cloud.xcan.sdf.validator;

import cloud.xcan.sdf.validator.impl.DomainValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see org.apache.commons.validator.routines.DomainValidator
 */
@Documented
@Constraint(validatedBy = {DomainValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Domain {

  String message() default "{xcan.validator.constraints.Domain.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
