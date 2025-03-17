package cloud.xcan.sdf.validator;

import cloud.xcan.sdf.validator.impl.EnumsPartValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EnumsPartValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumPart {

  String message() default "{xcan.validator.constraints.EnumPart.message}";

  Class<? extends Enum<?>> enumClass();

  String[] allowableValues() default {};

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean ignoreCase() default false;

}
