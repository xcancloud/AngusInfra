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
@JsonSerialize(using = DataSizeSerializer.class)
@JsonDeserialize(using = DataSizeDeSerializer.class)
public @interface DataSizeFormat {

  ValueUnitStyle value() default ValueUnitStyle.HUMAN;

}
