package cloud.xcan.angus.core.biz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable post-return filling of VO fields annotated with
 * {@link cloud.xcan.angus.remote.MessageJoinField} via {@link I18nMessageResolver}.
 *
 * <p>Prefer injecting {@link I18nMessageResolver} in assemblers for new code. Use this annotation
 * when a thin facade should assemble translated display text automatically.</p>
 *
 * <p>If the return type has no {@code @MessageJoinField}, the aspect no-ops.</p>
 *
 * @author XiaoLong Liu
 * @see I18nMessageResolver
 * @see cloud.xcan.angus.remote.MessageJoinField
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageJoin {

}
