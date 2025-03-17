package cloud.xcan.sdf.validator;

import cloud.xcan.sdf.validator.impl.HostnameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Constraint(validatedBy = {HostnameValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Hostname {

  String message() default "{xcan.validator.constraints.Hostname.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
