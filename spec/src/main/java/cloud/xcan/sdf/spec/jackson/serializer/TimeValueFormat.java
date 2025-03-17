package cloud.xcan.sdf.spec.jackson.serializer;

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
@JsonSerialize(using = TimeValueSerializer.class)
@JsonDeserialize(using = TimeValueDeSerializer.class)
public @interface TimeValueFormat {

  ValueUnitStyle value() default ValueUnitStyle.HUMAN;

}
