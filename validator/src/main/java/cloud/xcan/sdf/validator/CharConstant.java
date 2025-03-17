package cloud.xcan.sdf.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import cloud.xcan.sdf.validator.impl.CharConstantValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Constraint(validatedBy = CharConstantValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RUNTIME)
public @interface CharConstant {

  String[] array();

  String message() default "{xcan.validator.constraints.CharConstant.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
