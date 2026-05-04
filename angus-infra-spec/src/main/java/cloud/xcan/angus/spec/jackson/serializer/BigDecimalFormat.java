package cloud.xcan.angus.spec.jackson.serializer;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = BigDecimalSerializer.class)
@JsonDeserialize(using = BigDecimalDeSerializer.class)
public @interface BigDecimalFormat {

  /**
   * The default value, if the @BigDecimalFormat annotation is added or no value is specified, it
   * will be formatted in the following form
   */
  String value() default "0.00";

}
