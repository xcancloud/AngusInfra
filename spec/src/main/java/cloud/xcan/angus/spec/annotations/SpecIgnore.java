package cloud.xcan.angus.spec.annotations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marking an attribute that does not belong to the OAS3 specification - Not implemented in
 * Jackson.
 * <p>
 * Note: Annotation @SpecIgnore {@link SpecIgnore} is only not effective when generating
 * specifications and will not be ignored in data exchange. And @JsonIgnore {@link JsonIgnore} will
 * be ignored by default in generating specifications and data exchange.
 */
@Beta
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpecIgnore {

  boolean value() default true;
}
