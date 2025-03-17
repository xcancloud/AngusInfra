package cloud.xcan.sdf.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import cloud.xcan.sdf.validator.impl.CreditCardValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Checks if the string can be a valid credit card number.
 */
@Documented
@Constraint(validatedBy = CreditCardValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface CreditCard {

  String message() default "{xcan.validator.constraints.CreditCard.message}";

  CreditCardType[] value() default {CreditCardType.ALL};

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
