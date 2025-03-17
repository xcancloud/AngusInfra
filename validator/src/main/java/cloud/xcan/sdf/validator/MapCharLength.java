package cloud.xcan.sdf.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import cloud.xcan.sdf.validator.impl.MapCharLengthValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = MapCharLengthValidator.class)
public @interface MapCharLength {

  int keyMaxLength() default 100;

  int valueMaxLength() default 500;

  String message() default "{xcan.validator.constraints.MapCharLength.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({METHOD, FIELD, PARAMETER, ANNOTATION_TYPE})
  @Retention(RUNTIME)
  @Documented
  public @interface List {

    MapCharLength[] value();
  }
}
