package cloud.xcan.angus.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a repository method as requiring shard routing.
 *
 * <p>The {@link #shardKey()} specifies which field in the method arguments determines the
 * database shard. The {@link #tableKey()} specifies which field determines the table shard
 * (secondary index). When empty, the same key as {@link #shardKey()} is used.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sharding {

  /**
   * The field name used to resolve the database shard index. Defaults to empty, meaning the
   * framework will look for the first {@code Long} argument.
   */
  String shardKey() default "";

  /**
   * The field name used to resolve the table secondary index. Defaults to empty, meaning table
   * sharding uses the same key as {@link #shardKey()}.
   */
  String tableKey() default "";
}
