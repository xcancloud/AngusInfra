package cloud.xcan.sdf.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NameJoinField {

  /**
   * Query repository bean name
   */
  String repository();

  /**
   * Query ID field name
   */
  String id();

}
