package cloud.xcan.angus.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import cloud.xcan.angus.validator.impl.AlphanumericValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Checks if the String contains only unicode letters or digits.
 */
@Documented
@Constraint(validatedBy = AlphanumericValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Alphanumeric {

  String message() default "{xcan.validator.constraints.string.Alphanumeric.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
