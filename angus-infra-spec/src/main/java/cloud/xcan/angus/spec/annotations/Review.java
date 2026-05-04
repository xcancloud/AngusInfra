package cloud.xcan.angus.spec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This makes note of something we know will change before release or are unsure about. By applying
 * this annotation and making sure all instances of it are removed before release, we will make sure
 * not to miss anything we intended to review.
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR,
    ElementType.METHOD})
public @interface Review {

  /**
   * An explanation of why we should review this before general availability. Will it definitely
   * change? Are we just testing something?
   */
  String value();
}
