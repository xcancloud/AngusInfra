package cloud.xcan.sdf.validator;

import cloud.xcan.sdf.validator.impl.PortValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {PortValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Port {

  long min() default PortValidator.MIN_PORT_VALUE;

  long max() default PortValidator.MAX_PORT_VALUE;

  String message() default "{xcan.validator.constraints.port.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
