package cloud.xcan.angus.remote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a VO field to be filled with a DB-backed i18n message after the method returns.
 *
 * <p>The stable message key is read from {@link #keyField()} when set; otherwise the annotated
 * field's current value is used as the key (then overwritten with the translated text).</p>
 *
 * @author XiaoLong Liu
 * @see cloud.xcan.angus.core.biz.MessageJoin
 * @see cloud.xcan.angus.core.biz.I18nMessageResolver
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageJoinField {

  /**
   * Message category matching {@code gm_i18n_messages.type}.
   */
  String type();

  /**
   * Name of the VO field that holds the stable message key. Empty means use this field's value.
   */
  String keyField() default "";

  /**
   * When {@code true}, load/warm the type-level Caffeine cache. When {@code false}, query only the
   * keys present on the current page.
   */
  boolean enabledCache() default true;

}
