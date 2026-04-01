package cloud.xcan.angus.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity as a sharded table. Entity table names annotated with this annotation
 * will be rewritten at SQL level to include the tenant and optional table index suffix.
 *
 * <p>For example, a table named {@code exec_sample} for tenant 100 becomes {@code exec_sample-100}
 * or {@code exec_sample-100-3} when secondary table indexing is enabled.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ShardedTable {

}
